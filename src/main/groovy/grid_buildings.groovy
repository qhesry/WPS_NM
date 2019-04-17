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
        title = "grid buildings",
        description = "Make an grid around buildings",
        keywords = ["NM","buildings","Grid","Receivers"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:NM:BuildingsGrid"
)
def processing() {
        String outputTableName = "gridBuildings"
	String deleteIfExistGrid = String.format("DROP TABLE IF EXISTS %s", outputTableName)
        sql.execute(deleteIfExistGrid)

        sql.execute("drop table if exists receivers_build_0;")
        sql.execute("create table receivers_build_0 as SELECT "+buildingsPrimaryKey[0]+", ST_ExteriorRing(ST_Buffer(ST_SimplifyPreserveTopology(b.the_geom,2), 2, 'quad_segs=0 endcap=flat')) the_geom  from "+buildingTableName+" b ;")
        sql.execute("ALTER TABLE receivers_build_0 ADD COLUMN id SERIAL PRIMARY KEY;")

        sql.execute("CREATE TABLE "+outputTableName+" AS SELECT "+buildingsPrimaryKey[0]+", the_geom FROM receivers_build_0; ")
        sql.execute("drop table if exists indexed_points;")
        sql.execute("create table indexed_points(old_edge_id integer, the_geom geometry, number_on_line integer, gid integer);")

        sql.execute("drop table if exists bb;")
        sql.execute("create table bb as select ST_EXPAND(ST_Collect(ST_ACCUM(b.the_geom)),-2000,-2000)  the_geom from exbuildings_selection b;")

        sql.execute("drop table if exists receivers_build_ratio;")
        sql.execute("create table receivers_build_ratio as select a.* from receivers_build_0 a, bb b where st_intersects(b.the_geom, a.the_geom) ORDER BY random() LIMIT 10;")

        sql.execute("drop table if exists indexed_points;")
        sql.execute("create table indexed_points(old_edge_id integer, the_geom geometry, number_on_line integer, gid integer);")

        current_fractional = 0.0
        current_number_of_point = 1
        def insertInIndexedPoints = "INSERT INTO indexed_points(old_edge_id, the_geom, number_on_line, gid) VALUES (?, ST_LocateAlong(?, ?), ?, ?);"

        sql.eachRow("SELECT id as id_column, st_transform(the_geom, 2154) as the_geom, "+ buildingsPrimaryKey[0]+" as build_id, " +
                "st_length(st_transform(the_geom, 2154)) as line_length FROM receivers_build_ratio;"){ row ->
                current_fractional = 0.0;
                while(current_fractional <= 1.0){
                        sql.withBatch(insertInIndexedPoints) { batch ->
                                batch.addBatch(row.id_column, row.the_geom, current_fractional, current_number_of_point, row.build_id)
                        }
                        current_fractional = current_fractional + (5 / data[i].line_length);
                        current_number_of_point = current_number_of_point + 1
                }
        }
        
        sql.execute("ALTER TABLE indexed_points ADD COLUMN id SERIAL PRIMARY KEY;")

        sql.execute("drop table if exists receivers_delete;")
        sql.execute("create table receivers_delete as SELECT r.ID, r.the_geom,r.gid build_id from indexed_points r, "+buildingTableName+" b where st_intersects(b.the_geom, r.the_geom);")
        sql.execute("delete from indexed_points r where exists (select 1 from receivers_delete rd where r.ID=rd.ID);")
        sql.execute("drop table if exists receivers_delete;")

        sql.execute("alter table indexed_points DROP column id ;")
        sql.execute("alter table indexed_points add column id serial ;")

        sql.execute("drop table if exists receivers;")
        sql.execute("create table receivers as select id id, gid gid, ST_Translate(ST_force3d(the_geom),0,0,4) the_geom from indexed_points;")

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
        title = "Primary key Buildings",
        description = "The primary key column of the buildings table.",
        jdbcTableReference = "BuildingTableName")
String[] buildingsPrimaryKey

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

