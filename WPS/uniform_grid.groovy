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

        sql.execute("delete from grid g where exists (select 1 from "+buildingTableName+" b where g.the_geom && b.the_geom and ST_distance(b.the_geom, g.the_geom) < 1 limit 1);")

        sql.execute("delete from grid g where exists (select 1 from "+roadsTableName+" r where st_expand(g.the_geom, 1) && r.the_geom and st_distance(g.the_geom, r.the_geom) < 1 limit 1);")

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

