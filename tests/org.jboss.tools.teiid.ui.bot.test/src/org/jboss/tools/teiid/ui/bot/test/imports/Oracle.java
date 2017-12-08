package org.jboss.tools.teiid.ui.bot.test.imports;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.reddeer.eclipse.condition.ConsoleHasText;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.impl.menu.ShellMenuItem;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.teiid.reddeer.ImportHelper;
import org.jboss.tools.teiid.reddeer.connection.ConnectionProfileConstants;
import org.jboss.tools.teiid.reddeer.connection.ResourceFileHelper;
import org.jboss.tools.teiid.reddeer.connection.TeiidJDBCHelper;
import org.jboss.tools.teiid.reddeer.editor.RelationalModelEditor;
import org.jboss.tools.teiid.reddeer.perspective.TeiidPerspective;
import org.jboss.tools.teiid.reddeer.preference.TeiidDesignerPreferencePage;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement.TeiidServer;
import org.jboss.tools.teiid.reddeer.view.ModelExplorer;
import org.jboss.tools.teiid.reddeer.view.ServersViewExt;
import org.jboss.tools.teiid.reddeer.wizard.imports.TeiidConnectionImportWizard;
import org.jboss.tools.teiid.reddeer.wizard.newWizard.VdbWizard;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(RedDeerSuite.class)
@OpenPerspective(TeiidPerspective.class)
@TeiidServer(state = ServerRequirementState.RUNNING, connectionProfiles = {
		ConnectionProfileConstants.ORACLE_11G_BQT2,
		ConnectionProfileConstants.ORACLE_11G_BOOKS,
		ConnectionProfileConstants.ORACLE_12C_BQT})
public class Oracle {
	@InjectRequirement
	private static TeiidServerRequirement teiidServer;	
	
	public ImportHelper importHelper = null;

	private static final String PROJECT_NAME_JDBC = "jdbcImportTest";
	private static final String PROJECT_NAME_TEIID = "TeiidConnImporter";
	
	private static final String UPDATE_QUERY = new ResourceFileHelper().getSql("JDBCImportWizardTest/updateBook").replaceAll("\r|\n", " ");
	private static final String UPDATE_DISALLOW_QUERY = new ResourceFileHelper().getSql("JDBCImportWizardTest/disallowUpdateBook").replaceAll("\r|\n", " ");
	private static final String UPDATE_DEFAULT_QUERY = new ResourceFileHelper().getSql("JDBCImportWizardTest/updateDefault").replaceAll("\r|\n", " ");

    private static final String MODEL_NAME_ORACLE_11_G = "oracle11gModel";
    private static final String MODEL_NAME_ORACLE_11_G_BOOKS = "oracle11gModelPackage";
    private static final String MODEL_NAME_ORACLE_12 = "oracle12cModel";

	@Before
	public void before() {
		if (new ShellMenuItem(new WorkbenchShell(), "Project", "Build Automatically").isSelected()) {
			new ShellMenuItem(new WorkbenchShell(), "Project", "Build Automatically").select();
		}
		new ModelExplorer().createProject(PROJECT_NAME_JDBC);
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		new TeiidDesignerPreferencePage(preferences).setTeiidConnectionImporterTimeout(240);
		new ModelExplorer().importProject(PROJECT_NAME_TEIID);
		new ModelExplorer().selectItem(PROJECT_NAME_TEIID);
		new ServersViewExt().refreshServer(teiidServer.getName());
		importHelper = new ImportHelper();
	}
	
	@After
	public void after(){
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + MODEL_NAME_ORACLE_11_G);
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + MODEL_NAME_ORACLE_11_G + "_DS");
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/Check_" + MODEL_NAME_ORACLE_11_G);

        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + MODEL_NAME_ORACLE_11_G_BOOKS);
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + MODEL_NAME_ORACLE_11_G_BOOKS + "_DS");
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/Check_" + MODEL_NAME_ORACLE_11_G_BOOKS);

        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + MODEL_NAME_ORACLE_12);
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/" + MODEL_NAME_ORACLE_12 + "_DS");
        new ServersViewExt().deleteDatasource(teiidServer.getName(), "java:/Check_" + MODEL_NAME_ORACLE_12);

		new ModelExplorer().deleteAllProjectsSafely();
	}	

	@Test
	public void oracle11gJDBCtest() {
		importHelper.importModelJDBC(PROJECT_NAME_JDBC, MODEL_NAME_ORACLE_11_G, ConnectionProfileConstants.ORACLE_11G_BQT2, "BQT2/TABLE/SMALLA,BQT2/TABLE/SMALLB", false);
		new RelationalModelEditor(MODEL_NAME_ORACLE_11_G + ".xmi").save();
		importHelper.checkImportedTablesInModelJDBC(PROJECT_NAME_JDBC, MODEL_NAME_ORACLE_11_G, "SMALLA", "SMALLB", teiidServer);
	}
	
	@Test
	public void oracle11gPackageJDBCtest() {
		importHelper.importModelJDBC(PROJECT_NAME_JDBC, MODEL_NAME_ORACLE_11_G_BOOKS, ConnectionProfileConstants.ORACLE_11G_BOOKS, "BOOKS/procedure/REMOVE_AUTHOR2", true);
		new RelationalModelEditor(MODEL_NAME_ORACLE_11_G_BOOKS + ".xmi").save();
		importHelper.checkImportedProcedureInModelJDBC(PROJECT_NAME_JDBC, MODEL_NAME_ORACLE_11_G_BOOKS,"REMOVE_AUTHOR2", teiidServer, "90" );
	}

	@Test
	public void oracle12cJDBCtest() {
		importHelper.importModelJDBC(PROJECT_NAME_JDBC, MODEL_NAME_ORACLE_12, ConnectionProfileConstants.ORACLE_12C_BQT, "DV/TABLE/SMALLA,DV/TABLE/SMALLB", false);
		new RelationalModelEditor(MODEL_NAME_ORACLE_12 + ".xmi").save();
		importHelper.checkImportedTablesInModelJDBC(PROJECT_NAME_JDBC, MODEL_NAME_ORACLE_12, "SMALLA", "SMALLB", teiidServer);
	}
	
	@Test
	public void oracle11gTeiidTest() {
		Map<String,String> teiidImporterProperties = new HashMap<String, String>();
		teiidImporterProperties.put(TeiidConnectionImportWizard.IMPORT_PROPERTY_TABLE_NAME_PATTERN, "SMALL%");
		teiidImporterProperties.put(TeiidConnectionImportWizard.IMPORT_PROPERTY_SCHEMA_PATTERN, "BQT2");
		importHelper.importModelTeiid(PROJECT_NAME_TEIID, ConnectionProfileConstants.ORACLE_11G_BQT2, MODEL_NAME_ORACLE_11_G, teiidImporterProperties,teiidServer);
		importHelper.checkImportedModelTeiid(PROJECT_NAME_TEIID, MODEL_NAME_ORACLE_11_G, "SMALLA", "SMALLB");
	}

	@Test
	public void oracle12cTeiidTest() {
		Map<String,String> teiidImporterProperties = new HashMap<String, String>();
		teiidImporterProperties.put(TeiidConnectionImportWizard.IMPORT_PROPERTY_TABLE_NAME_PATTERN, "SMALL%");
		importHelper.importModelTeiid(PROJECT_NAME_TEIID, ConnectionProfileConstants.ORACLE_12C_BQT, MODEL_NAME_ORACLE_12, teiidImporterProperties, teiidServer);
		importHelper.checkImportedModelTeiid(PROJECT_NAME_TEIID, MODEL_NAME_ORACLE_12, "SMALLA", "SMALLB");
	}	
	@Test
	/* Test if updatable value is set correctly after import */
	public void updatableModelJDBCtest(){
		String updatableModel = "updatableModel";
		String notUpdatableModel = "notUpdatableModel";
		
		importHelper.importModelJDBC(PROJECT_NAME_JDBC, updatableModel, ConnectionProfileConstants.ORACLE_11G_BOOKS, null, false, true);
		new RelationalModelEditor(updatableModel + ".xmi").save();
		assertTrue(importHelper.checkUpdatableModelJDBC(PROJECT_NAME_JDBC, updatableModel,true));
		
		importHelper.importModelJDBC(PROJECT_NAME_JDBC, notUpdatableModel, ConnectionProfileConstants.ORACLE_11G_BOOKS, null, false, false);
		new RelationalModelEditor(notUpdatableModel + ".xmi").save();
		assertTrue(importHelper.checkUpdatableModelJDBC(PROJECT_NAME_JDBC, notUpdatableModel,false));
		VdbWizard.openVdbWizard()
				.setName(updatableModel+"Vdb")
				.addModel(PROJECT_NAME_JDBC,updatableModel)
				.finish();
		new ModelExplorer().deployVdb(PROJECT_NAME_JDBC, updatableModel+"Vdb");
		
		VdbWizard.openVdbWizard()
				.setName(notUpdatableModel+"Vdb")
				.addModel(PROJECT_NAME_JDBC,notUpdatableModel)
				.finish();
		new ModelExplorer().deployVdb(PROJECT_NAME_JDBC, notUpdatableModel+"Vdb");

		try{
			TeiidJDBCHelper jdbcHelper = new TeiidJDBCHelper(teiidServer, updatableModel+"Vdb");
			assertTrue(jdbcHelper.isQuerySuccessful(UPDATE_QUERY,false));
			jdbcHelper = new TeiidJDBCHelper(teiidServer, notUpdatableModel+"Vdb");
			assertFalse(jdbcHelper.isQuerySuccessful(UPDATE_DISALLOW_QUERY,false));
			assertTrue(new ConsoleHasText("TEIID30492 Metadata does not allow updates on the group: "+ notUpdatableModel +".AUTHORS").test());
		}catch(Exception e){
 			fail(e.getMessage());
		}finally{
			TeiidJDBCHelper jdbcHelper = new TeiidJDBCHelper(teiidServer, updatableModel+"Vdb");
			jdbcHelper.isQuerySuccessful(UPDATE_DEFAULT_QUERY,false);
		}
	}
}

