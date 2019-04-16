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
        String outputTableName = "grid"
	String deleteIfExistGrid = String.format("DROP TABLE IF EXISTS %s", outputTableName)
        sql.execute(deleteIfExistGrid)

        def min_max = sql.firstRow("SELECT ST_XMAX(the_geom) as maxX, ST_XMIN(the_geom) as minX, ST_YMAX(the_geom) as maxY, ST_YMIN(the_geom) as minY"
        +" FROM "
        +"("
                +" SELECT ST_Collect(the_geom) as the_geom "
                +" FROM " + roadsTableName
                +" UNION ALL "
                +" SELECT the_geom " 
                +" FROM " + buildingTableName
        +");")

        sql.execute("create table "+outputTableName+" as select ST_MAKEPOINT(RAND()*("+min_max.maxX+" - "+min_max.minX+") + "+min_max.minX+", RAND()*("+min_max.maxY+" - "+min_max.minY+") + "+min_max.minY+") as the_geom from system_range(0,"+numberOfPoints+");")

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

@LiteralDataInput(
    title = "Number of points",
    description = "The number of random points generated",
    minOccurs = 1)
String numberOfPoints = "50"

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

