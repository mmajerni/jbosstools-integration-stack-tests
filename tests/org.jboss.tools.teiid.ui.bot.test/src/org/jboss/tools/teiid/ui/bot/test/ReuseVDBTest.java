package org.jboss.tools.teiid.ui.bot.test;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.junit.requirement.inject.InjectRequirement;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
import org.eclipse.reddeer.requirements.server.ServerRequirementState;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.menu.ShellMenuItem;
import org.eclipse.reddeer.swt.impl.tree.DefaultTreeItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.common.reddeer.JiraClient;
import org.jboss.tools.teiid.reddeer.condition.IsInProgress;
import org.jboss.tools.teiid.reddeer.connection.ConnectionProfileConstants;
import org.jboss.tools.teiid.reddeer.connection.TeiidJDBCHelper;
import org.jboss.tools.teiid.reddeer.editor.ModelEditor;
import org.jboss.tools.teiid.reddeer.editor.RelationalModelEditor;
import org.jboss.tools.teiid.reddeer.editor.TransformationEditor;
import org.jboss.tools.teiid.reddeer.editor.VdbEditor;
import org.jboss.tools.teiid.reddeer.perspective.TeiidPerspective;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement;
import org.jboss.tools.teiid.reddeer.requirement.TeiidServerRequirement.TeiidServer;
import org.jboss.tools.teiid.reddeer.view.ModelExplorer;
import org.jboss.tools.teiid.reddeer.wizard.imports.ImportJDBCDatabaseWizard;
import org.jboss.tools.teiid.reddeer.wizard.newWizard.MetadataModelWizard;
import org.jboss.tools.teiid.reddeer.wizard.newWizard.VdbWizard;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author mkralik
 */

@RunWith(RedDeerSuite.class)
@OpenPerspective(TeiidPerspective.class)
@TeiidServer(state = ServerRequirementState.RUNNING, connectionProfiles={
		ConnectionProfileConstants.SQL_SERVER_2008_PARTS_SUPPLIER,
})

public class ReuseVDBTest {
	private static final String PROJECT_NAME = "reuseVDB";
	private static final String NAME_ORACLE_MODEL = "sourceModel";
	private static final String VIEW_SOURCE_MODEL = "viewFromSource";
	private static final String SOURCE_VDB = "sourceVDB";
	
	private static final String PROJECT_NAME_REUSE = "reuseVDBProject";
	private static final String VIEW_REUSE_MODEL = "viewReuse";
	private static final String REUSE_VDB = "reuseVDBname";

	@InjectRequirement
	private static TeiidServerRequirement teiidServer;
	
	private static ModelExplorer modelExplorer;
	
	@BeforeClass
	public static void before() {
		modelExplorer = new ModelExplorer();
		modelExplorer.importProject(PROJECT_NAME);
		modelExplorer.changeConnectionProfile(ConnectionProfileConstants.SQL_SERVER_2008_PARTS_SUPPLIER, PROJECT_NAME, NAME_ORACLE_MODEL);
	}
	
	@Test
	public void reuseVDBtest(){
		VdbWizard.openVdbWizard()
				.setLocation(PROJECT_NAME)
				.setName(SOURCE_VDB)
				.addModel(PROJECT_NAME,VIEW_SOURCE_MODEL + ".xmi")
				.finish();
		executeVDB(PROJECT_NAME, SOURCE_VDB);
		
		modelExplorer.createProject(PROJECT_NAME_REUSE);
		ImportJDBCDatabaseWizard.openWizard()
				.setConnectionProfile(SOURCE_VDB + " - localhost - Teiid Connection")
				.nextPage()
				.setTableTypes(false, true, false)
				.nextPage()
				.setTables(VIEW_SOURCE_MODEL)
				.nextPage()
				.setFolder(PROJECT_NAME_REUSE)
				.finish();
		
		MetadataModelWizard wizard = MetadataModelWizard.openWizard()
				.setLocation(PROJECT_NAME_REUSE)
				.setModelName(VIEW_REUSE_MODEL)
				.selectModelClass(MetadataModelWizard.ModelClass.RELATIONAL)
				.selectModelType(MetadataModelWizard.ModelType.VIEW)
				.selectModelBuilder(MetadataModelWizard.ModelBuilder.TRANSFORM_EXISTING)
				.nextPage();
		try{
			new PushButton("OK").click();
		}catch(Exception ex){
			
		}
		wizard.activateWizard().finish();
		new RelationalModelEditor(VIEW_REUSE_MODEL + ".xmi").save();

		VdbWizard.openVdbWizard()
				.setLocation(PROJECT_NAME_REUSE)
				.setName(REUSE_VDB)
				.addModel(PROJECT_NAME_REUSE,VIEW_REUSE_MODEL + ".xmi")
				.finish();
		modelExplorer.deployVdb(PROJECT_NAME_REUSE, REUSE_VDB);

		/* test version 1 */
		TeiidJDBCHelper jdbchelper = new TeiidJDBCHelper(teiidServer, REUSE_VDB);
		try {
			ResultSet rs = jdbchelper.executeQueryWithResultSet("SELECT * FROM "+VIEW_REUSE_MODEL+".version");
			rs.next();
			assertEquals("version1", rs.getString(1));
			assertEquals(16,jdbchelper.getNumberOfResults("SELECT * FROM "+VIEW_REUSE_MODEL+".PARTS"));
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			jdbchelper.closeConnection();
		}
		/*change sourceVDB version to 2*/
		new RelationalModelEditor(VIEW_SOURCE_MODEL + ".xmi").close();
		modelExplorer.openModelEditor(PROJECT_NAME,VIEW_SOURCE_MODEL+".xmi");		
		TransformationEditor transformation = new RelationalModelEditor(VIEW_SOURCE_MODEL + ".xmi").openTransformationDiagram(ModelEditor.ItemType.TABLE, "version");
		transformation.setTransformation("SELECT 'version2'");
		transformation.saveAndValidateSql();
		
		AbstractWait.sleep(TimePeriod.DEFAULT);
		new ShellMenuItem(new WorkbenchShell(), "File", "Save All").select();
		VdbEditor vdb = new VdbEditor(SOURCE_VDB + ".vdb");
		vdb.activate();
		vdb.synchronizeAll();
		vdb.setVersion(2);
		new RelationalModelEditor(SOURCE_VDB + ".vdb").save();
		modelExplorer.deployVdb(PROJECT_NAME, SOURCE_VDB);
		
		/* test version 1 */
		if(new JiraClient().isIssueClosed("TEIIDDES-2848")){
			jdbchelper = new TeiidJDBCHelper(teiidServer, REUSE_VDB);
			try {
				ResultSet rs = jdbchelper.executeQueryWithResultSet("SELECT * FROM "+VIEW_REUSE_MODEL+".version");
				rs.next();
				assertEquals("version1", rs.getString(1));  
				assertEquals(17,jdbchelper.getNumberOfResults("SELECT * FROM "+VIEW_REUSE_MODEL+".PARTS"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		/* change and test reuseVDB with version 2 */
		vdb = new VdbEditor(REUSE_VDB + ".vdb");
		vdb.activate();
		vdb.setImportVDB(SOURCE_VDB,2,false);
		new RelationalModelEditor(REUSE_VDB + ".vdb").save();
		
		modelExplorer.deployVdb(PROJECT_NAME_REUSE, REUSE_VDB);
		
		jdbchelper = new TeiidJDBCHelper(teiidServer, REUSE_VDB);
		try {
			ResultSet rs = jdbchelper.executeQueryWithResultSet("SELECT * FROM "+VIEW_REUSE_MODEL+".version");
			rs.next();
			assertEquals("version2", rs.getString(1));  
			assertEquals(16,jdbchelper.getNumberOfResults("SELECT * FROM "+VIEW_REUSE_MODEL+".PARTS"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * deploy vdb and create connection profile (modelExplorer.deployVDB() doesn't create CP)
	 */
	private void executeVDB(String project, String vdb) {
 		modelExplorer.activate();
 		vdb = (vdb.contains(".vdb")) ? vdb : vdb + ".vdb";
 		
 		new DefaultTreeItem(project, vdb).select();
 		new ContextMenuItem("Modeling", "Execute VDB").select();
 
 		new WaitWhile(new IsInProgress(), TimePeriod.VERY_LONG);
 		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
 		new WorkbenchShell();
 		TeiidPerspective.activate();
 	}
}
