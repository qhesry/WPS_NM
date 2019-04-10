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
        title = "Random Grid",
        description = "Make an Random grid receivers",
        keywords = ["NM","Random","Grid","Receivers"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:NM:RandomGrid"
)
def processing() {

        sql.execute("SET @XMAX = SELECT ST_XMAX(THE_GEOM) FROM "+ buildingTableName+";")
        sql.execute("SET @YMAX = SELECT ST_YMAX(THE_GEOM) FROM "+ buildingTableName+";")
        sql.execute("SET @XMIN = SELECT ST_XMIN(THE_GEOM) FROM "+ buildingTableName+";")
        sql.execute("SET @YMIN = SELECT ST_YMIN(THE_GEOM) FROM "+ buildingTableName+";")
	String outputTableName = "grid"
	String deleteIfExistGrid = String.format("DROP TABLE IF EXISTS %s", outputTableName)

        sql.execute(deleteIfExistGrid)
        sql.execute("create table "+outputTableName+ " as select ST_MAKEPOINT(RAND()*(@XMAX - @XMIN) + @XMIN, RAND()*(@YMAX - @YMIN) + @YMIN);")

        sql.execute("delete from "+outputTableName+ " g where exists (select 1 from "+buildingTableName+" b where g.the_geom && b.the_geom and ST_distance(b.the_geom, g.the_geom) < 1 limit 1);")

        sql.execute("delete from "+outputTableName+ " g where exists (select 1 from "+roadsTableName+" r where st_expand(g.the_geom, 1) && r.the_geom and st_distance(g.the_geom, r.the_geom) < 1 limit 1);")

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

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

