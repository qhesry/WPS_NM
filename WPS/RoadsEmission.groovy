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
        title = "NoiseModelling Emission",
        description = "Compute Emission",
        keywords = ["NM","Emission"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:NM:ComputeEmission"
)
def processing() {
	String outputTableName = "roads_src_global"
	String deleteIfExist = String.format("DROP TABLE IF EXISTS %s", outputTableName)

    String query = String.format("CREATE TABLE %s AS SELECT "
	+ primaryKey[0] + ",BR_EvalSource(" 
	+ loadSpeed[0] 
	+ ", " + lightVehicleCount[0] 
	+ ", "+ heavyVehicleCount[0] 
	+ ", " + speedJunction[0] 
	+ ", " + maxSpeed[0] 
	+ ", " + roadType[0] 
	+ ", ST_Z(ST_GeometryN(ST_ToMultiPoint(" + primaryKey[0] + "),1))"
	+ ", ST_Z(ST_GeometryN(ST_ToMultiPoint(" + primaryKey[0] + "),2))"
	+ ", ST_Length(" + primaryKey[0] + "), False)"
	+ "as db_m from %s;", outputTableName, inputTableName)

	sql.execute(deleteIfExist)
	sql.execute(query)

    literalOutput = i18n.tr("Process done !")
}


/****************/
/** INPUT Data **/
/****************/

/** This JDBCTable is the input model source table. */
@JDBCTableInput(
        title = "Table to compute",
        description = "The table with the roads geometry and traffic",
        identifier = "inputTableName"
)
String inputTableName

/**********************/
/** INPUT Parameters **/
/**********************/
@JDBCColumnInput(
        title = "Primary key Column",
        description = "The primary key column of the input table.",
        jdbcTableReference = "inputTableName")
String[] primaryKey

@JDBCColumnInput(
        title = "Road type Column",
        description = "The road type column of the input table.",
        jdbcTableReference = "inputTableName")
String[] roadType

@JDBCColumnInput(
        title = "Load Speed Column",
        description = "The load speed column of the input table.",
        jdbcTableReference = "inputTableName")
String[] loadSpeed

@JDBCColumnInput(
        title = "Speed Junction Column",
        description = "The speed junction column of the input table.",
        jdbcTableReference = "inputTableName")
String[] speedJunction

@JDBCColumnInput(
        title = "Max Speed Column",
        description = "The road type column of the input table.",
        jdbcTableReference = "inputTableName")
String[] maxSpeed

@JDBCColumnInput(
        title = "Light Vehicle Count Column",
        description = "The light vehicle count column of the input table.",
        jdbcTableReference = "inputTableName")
String[] lightVehicleCount

@JDBCColumnInput(
        title = "Heavy Vehicle Count Column",
        description = "The heavy vehicle count column of the input table.",
        jdbcTableReference = "inputTableName")
String[] heavyVehicleCount

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

