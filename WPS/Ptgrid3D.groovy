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
        title = "Noise Propagation (ptgrid3D)",
        description = "Compute Propagation",
        keywords = ["NM","Propagation"],
        properties = ["DBMS_TYPE", "H2GIS", "DBMS_TYPE", "POSTGIS"],
        version = "1.0",
        identifier = "orbisgis:wps:NM:ComputePropagation"
)
def processing() {
	String outputTableName = "pt_level"
	String deleteIfExist = String.format("DROP TABLE IF EXISTS %s", outputTableName)
    
	String query = String.format("create table %s as SELECT * from BR_PtGrid3D('" 
	+ buildingTableName + "','"
	+ heightFieldName[0] + "','"
	+ sourceTableName + "','"
	+ receiversTableName + "','" 
	+ sourceFieldPrefix + "','"
	+ groundTableName + "','"
	+ demTableName + "',"
	+ maximumPropagationDistance + ","
	+ maximumWallSeekingDistance + ","
	+ soundReflectionOrder + "," 
	+ soundDiffrationOrder + "," 
	+ wallAlpha + ");"
	, outputTableName)

	sql.execute(deleteIfExist)
	sql.execute(query)

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
	title = "Height Field of Buildings",
	description = "The field name of the height in the building table",
	jdbcTableReference = "BuildingTableName")
String[] heightFieldName

@JDBCTableInput(
	title = "Source Table",
	description = "The table with the sources",
	identifier = "SourceTableName")
String sourceTableName

@LiteralDataInput(
    title = "Source Field Prefix",
    description = "The prefix of the sources column",
    minOccurs = 1)
String sourceFieldPrefix

@JDBCTableInput(
	title = "Receivers Table",
	description = "The table with the receivers",
	identifier = "ReceiversTableName")
String receiversTableName

@JDBCTableInput(
	title = "Ground Type Table",
	description = "The table with the ground",
	identifier = "GroundTableName")
String groundTableName

@JDBCTableInput(
	title = "Dem Table",
	description = "The table with the dem",
	identifier = "DemTableName")
String demTableName

@LiteralDataInput(
    title = "Maximum Propagation Distance",
    description = "The maximum propagation distance",
    minOccurs = 1)
String maximumPropagationDistance = "750"

@LiteralDataInput(
    title = "Maximum Wall Seeking Distance",
    description = "The maximum wall seeking distance",
    minOccurs = 1)
String maximumWallSeekingDistance = "50"

@LiteralDataInput(
    title = "Sound Reflection Order",
    description = "The sound reflection order",
    minOccurs = 1)
String soundReflectionOrder = "2"

@LiteralDataInput(
    title = "Sound Diffraction Order",
    description = "The sound diffraction order",
    minOccurs = 0)
String soundDiffrationOrder = "1"

@LiteralDataInput(
    title = "Wall Alpha",
    description = "The wall alpha",
    minOccurs = 1)
String wallAlpha = "0.23"

/** Output message. */
@LiteralDataOutput(
        title = "Output message",
        description = "The output message.",
        identifier = "literalOutput")
String literalOutput

