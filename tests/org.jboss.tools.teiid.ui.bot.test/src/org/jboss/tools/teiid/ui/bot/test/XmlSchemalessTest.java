package org.jboss.tools.teiid.ui.bot.test;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.teiid.reddeer.connection.ConnectionProfileConstants;
import org.jboss.tools.teiid.reddeer.connection.ResourceFileHelper;
import org.jboss.tools.teiid.reddeer.connection.TeiidJDBCHelper;
import org.jboss.tools.teiid.reddeer.dialog.XmlDocumentBuilderDialog;
import org.jboss.tools.teiid.reddeer.editor.TableEditor;
import org.jboss.tools.teiid.reddeer.editor.TransformationEditor;
import org.jboss.tools.teiid.reddeer.editor.VdbEditor;
import org.jboss.tools.teiid.reddeer.editor.XmlModelEditor;
import org.jboss.tools.teiid.reddeer.perspective.TeiidPerspective;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement.TeiidServer;
import org.jboss.tools.teiid.reddeer.view.ModelExplorer;
import org.jboss.tools.teiid.reddeer.view.ProblemsViewEx;
import org.jboss.tools.teiid.reddeer.wizard.newWizard.MetadataModelWizard;
import org.jboss.tools.teiid.reddeer.wizard.newWizard.VdbWizard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author skaleta
 */
@RunWith(RedDeerSuite.class)
@OpenPerspective(TeiidPerspective.class)
@TeiidServer(state = ServerRequirementState.RUNNING, connectionProfiles = {ConnectionProfileConstants.ORACLE_11G_BOOKS})
public class XmlSchemalessTest {
	private static final String PROJECT_NAME = "XmlSchemalessProject";
	private static final String VIEW_MODEL = "BooksXml.xmi";
	private static final String VDB_NAME = "XmlSchemalessVdb";
	
	@InjectRequirement
	private static TeiidServerRequirement teiidServer;
	
	private ModelExplorer modelExplorer;
	private ResourceFileHelper fileHelper;
	private TeiidJDBCHelper jdbcHelper;
	
	@Before
	public  void importProject() {
		new WorkbenchShell().maximize();
		modelExplorer = new ModelExplorer();
		modelExplorer.importProject(PROJECT_NAME);
		modelExplorer.refreshProject(PROJECT_NAME);
		modelExplorer.changeConnectionProfile(ConnectionProfileConstants.ORACLE_11G_BOOKS, PROJECT_NAME, "Books.xmi");
		fileHelper = new ResourceFileHelper();
		jdbcHelper = new TeiidJDBCHelper(teiidServer, VDB_NAME);
	}
	
	@Test
	public void test() throws SQLException {
		// 1. Create an XML document model
		MetadataModelWizard.openWizard()
				.setModelName(VIEW_MODEL.substring(0,8))
				.selectModelClass(MetadataModelWizard.ModelClass.XML)
		        .selectModelType(MetadataModelWizard.ModelType.VIEW)
				.finish();	
		
		String modelPath = PROJECT_NAME + "/" + VIEW_MODEL;
		modelExplorer.openModelEditor(modelPath.split("/"));
		XmlModelEditor editor = new XmlModelEditor(VIEW_MODEL);
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.XML_DOCUMENT, modelPath.split("/"));
		XmlDocumentBuilderDialog xmlDocumentBuilder = new XmlDocumentBuilderDialog();
		xmlDocumentBuilder.finish();
		modelExplorer.renameModelItem("bookListingDocument", (modelPath+"/NewXMLDocument").split("/"));
		
		AbstractWait.sleep(TimePeriod.SHORT);
		editor.save();
		
		// 2. Model the document structure
		String xmlStructureBuilt = "bookListingDocument";
		modelExplorer.renameModelItem("bookListing", (modelPath+"/"+xmlStructureBuilt+"/Root_Element").split("/"));
		xmlStructureBuilt += "/bookListing";
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.NAME_SPACE, (modelPath+"/"+xmlStructureBuilt).split("/"));

		editor.activate();
		TableEditor tableEditor = editor.openTableEditor();
		tableEditor.openTab(TableEditor.Tabs.XML_NAMESPACES);
		tableEditor.setCellText(0, "bookListing", "Prefix", "xsd");
		tableEditor.setCellText(0, "bookListing", "Uri", "http://www.w3.org/2001/XMLSchema");
		tableEditor.close();
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.SEQUENCE, (modelPath+"/"+xmlStructureBuilt).split("/"));
		xmlStructureBuilt += "/sequence";
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
		modelExplorer.renameModelItem("book", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
		xmlStructureBuilt += "/book";
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.SEQUENCE, (modelPath+"/"+xmlStructureBuilt).split("/"));
		xmlStructureBuilt += "/sequence";
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
		modelExplorer.renameModelItem("isbn", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
		modelExplorer.renameModelItem("title", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
		modelExplorer.renameModelItem("edition", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
		modelExplorer.renameModelItem("publisherName", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
		
		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
		modelExplorer.renameModelItem("publisherLocation", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));

		AbstractWait.sleep(TimePeriod.SHORT);
		editor.save();
		
		// 3. Define the transformation
		modelExplorer.refreshProject(PROJECT_NAME);
		editor.activate();;
		
		editor.createMappingClass("bookListing","sequence");
		editor.deleteAttribute("book", "book");
		editor.openMappingClass("book");
		TransformationEditor bookTransfEditor = editor.openTransformationEditor();
		bookTransfEditor.insertAndValidateSql(fileHelper.getSql("XmlSchemalessTest/Book"));
		bookTransfEditor.close();
		editor.returnToParentDiagram();
		
		AbstractWait.sleep(TimePeriod.SHORT);
		editor.save();

		ProblemsViewEx.checkErrors();
		
		// 4. Create a VDB and query
		VdbWizard.openVdbWizard()
				.setLocation(PROJECT_NAME)
				.setName(VDB_NAME)
				.addModel(PROJECT_NAME, VIEW_MODEL)
				.finish();
		modelExplorer.deployVdb(PROJECT_NAME, VDB_NAME);
		
		String output = jdbcHelper.executeQueryWithXmlStringResult("SELECT * FROM bookListingDocument");
		String expectedOutput = fileHelper.getXml("XmlSchemalessTest/BuiltDocument");
        assertEquals(output.replaceAll("\\s+",""), expectedOutput.replaceAll("\\s+",""));
	    
	    // 5. Expand the document	    
	    modelExplorer.addSiblingToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt+"/edition").split("/"));
 		modelExplorer.renameModelItem("authors", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
 		xmlStructureBuilt += "/authors";
 		
 		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.SEQUENCE, (modelPath+"/"+xmlStructureBuilt).split("/"));
 		xmlStructureBuilt += "/sequence";
 		
 		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
 		modelExplorer.renameModelItem("author", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
 		xmlStructureBuilt += "/author";
 		
 		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.SEQUENCE, (modelPath+"/"+xmlStructureBuilt).split("/"));
 		xmlStructureBuilt += "/sequence";
 		
 		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
 		modelExplorer.renameModelItem("lastName", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));
 		
 		modelExplorer.addChildToModelItem(ModelExplorer.ChildType.ELEMENT, (modelPath+"/"+xmlStructureBuilt).split("/"));
 		modelExplorer.renameModelItem("firstName", (modelPath+"/"+xmlStructureBuilt+"/NewXmlElement").split("/"));

 		AbstractWait.sleep(TimePeriod.SHORT);
 		editor.save();
		
		editor.createMappingClass("bookListing","sequence [MC: book]","book","sequence","authors","sequence");
		editor.deleteAttribute("author", "author");
		editor.openMappingClass("author");
		editor.openInputSetEditor()
				.addNewInputParam("book","isbn : string")
				.finish();
		TransformationEditor authTransfEditor = editor.openTransformationEditor();
		authTransfEditor.insertAndValidateSql(fileHelper.getSql("XmlSchemalessTest/Author"));
		authTransfEditor.close();
		editor.returnToParentDiagram();
		
		AbstractWait.sleep(TimePeriod.SHORT);
		editor.save();

		ProblemsViewEx.checkErrors();
 		
 		//6. Query the expanded model
 		VdbEditor.getInstance(VDB_NAME).synchronizeAll();
 		modelExplorer.deployVdb(PROJECT_NAME, VDB_NAME);
 		
 		output = jdbcHelper.executeQueryWithXmlStringResult("SELECT * FROM bookListingDocument");
 		expectedOutput = fileHelper.getXml("XmlSchemalessTest/BuiltDocumentExtended");	
        assertEquals(output.replaceAll("\\s+",""), expectedOutput.replaceAll("\\s+",""));
	}
}
