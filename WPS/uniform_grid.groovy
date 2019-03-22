package org.orbisgis.orbiswps.scripts.scripts.NoiseModelling

import org.orbisgis.orbiswps.groovyapi.input.*
import org.orbisgis.orbiswps.groovyapi.output.*
import org.orbisgis.orbiswps.groovyapi.process.*

/********************/
/** Process method **/
/********************/

/**
 * @author Quentin HESRY
 */
@Process(
        title = "Uniform Grid",
        description = "Make an uniform grid receivers",
        keywords = ["NM","Uniform","Grid","Receivers"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:NM:UniformGrid"
)
def processing() {
	String outputTableName = "grid"
	String deleteIfExistGrid = String.format("DROP TABLE IF EXISTS %s", outputTableName)

    String queryGrid = String.format("CREATE TABLE grid AS SELECT * FROM ST_MakeGridPoints('"
    + buildingTableName + "',"
    + deltaX + ","
    + deltaY + ");")

	sql.execute(deleteIfExistGrid)
	sql.execute(queryGrid)
    sql.execute("DROP TABLE IF EXISTS newGrid;")
    sql.execute("CREATE TABLE newGrid AS SELECT g.* FROM grid as G LEFT JOIN "+ buildingTableName + " AS b ON ST_intersects(b.the_geom, g.the_geom) WHERE b."+ pk_buildingTable[0] +" IS NULL;")

    literalOutput = i18n.tr("Process done !")
}

/**********************/
/** INPUT Parameters **/
/**********************/

@JDBCTableInput(
	title = "Building table",
	description = "The table with the buildings",
	identifier = "BuildingTableName")
String buildingTableName

@JDBCColumnInput(
        title = "Building Primary Key Field",
        description = "The primary key field column of the buildings table",
        jdbcTableReference = "BuildingTableName",
        identifier = "PkBuildingField"
)
String[] pk_buildingTable

@JDBCColumnInput(
        title = "Building Geometry Column",
        description = "The geometry column of the buildings table",
        jdbcTableReference = "BuildingTableName",
        identifier = "BuildingGeometryColumn"
)
String[] buildingGeometryColumn

@JDBCTableInput(
	title = "Road Table",
	description = "The table with the roads",
	identifier = "RoadsTableName")
String roadsTableName

@LiteralDataInput(
    title = "Distance in X",
    description = "The distance between the points in X ",
    minOccurs = 1)
String deltaX = "5"

@LiteralDataInput(
    title = "Distance in Y",
    description = "The distance between the points in Y ",
    minOccurs = 1)
String deltaY = "5"

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

