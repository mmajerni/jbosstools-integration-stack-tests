package org.jboss.tools.teiid.ui.bot.test;

import java.util.Arrays;

import org.eclipse.swtbot.swt.finder.SWTBotTestCase;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.jboss.reddeer.swt.impl.menu.ShellMenu;
import org.jboss.reddeer.swt.util.Bot;
import org.jboss.reddeer.swt.wait.TimePeriod;
import org.jboss.reddeer.swt.wait.WaitWhile;
import org.jboss.tools.teiid.reddeer.ModelProject;
import org.jboss.tools.teiid.reddeer.VDB;
import org.jboss.tools.teiid.reddeer.condition.IsInProgress;
import org.jboss.tools.teiid.reddeer.editor.CriteriaBuilder;
import org.jboss.tools.teiid.reddeer.editor.ModelEditor;
import org.jboss.tools.teiid.reddeer.editor.SQLScrapbookEditor;
import org.jboss.tools.teiid.reddeer.editor.VDBEditor;
import org.jboss.tools.teiid.reddeer.perspective.DatabaseDevelopmentPerspective;
import org.jboss.tools.teiid.reddeer.perspective.TeiidPerspective;
import org.jboss.tools.teiid.reddeer.view.GuidesView;
import org.jboss.tools.teiid.reddeer.view.ModelExplorer;
import org.jboss.tools.teiid.reddeer.view.ModelExplorerView;
import org.jboss.tools.teiid.reddeer.view.Procedure;
import org.jboss.tools.teiid.reddeer.view.SQLResult;
import org.jboss.tools.teiid.reddeer.wizard.CreateMetadataModel;
import org.jboss.tools.teiid.reddeer.wizard.CreateVDB;
import org.jboss.tools.teiid.reddeer.wizard.ImportJDBCDatabaseWizard;
import org.jboss.tools.teiid.reddeer.wizard.ModelProjectWizard;
import org.jboss.tools.teiid.ui.bot.test.requirement.PerspectiveRequirement.Perspective;
import org.jboss.tools.teiid.ui.bot.test.requirement.ServerRequirement.Server;
import org.jboss.tools.teiid.ui.bot.test.requirement.ServerRequirement.State;
import org.jboss.tools.teiid.ui.bot.test.requirement.ServerRequirement.Type;
import org.junit.Test;

/**
 * Test functions described in testscript E2eVirtualGroupTutorial.
 * In contrast to VirtualGroupTutorialTest, it uses Modelling action sets and creates preview.
 * @author Lucie Fabrikova, lfabriko@redhat.com
 *
 */
@Perspective(name = "Teiid Designer")//initialize tests in this perspective
@Server(type = Type.ALL, state = State.RUNNING)//uses info about server - swtbot.properties
public class VirtualGroupTutorialUpdatedTest extends SWTBotTestCase{

	private static final String PROJECT_NAME = "MyFirstProject";

	private static TeiidBot teiidBot = new TeiidBot();
	
	private String jdbcProfile = "HSQLDB Profile";
	private String jdbcProfile2 = "HSQLDB Profile 2";
	
	private String SOURCE_MODEL_1 = "partssupModel1.xmi";
	private String SOURCE_MODEL_2 = "partssupModel2.xmi";
	
	private String props1 = "resources/db/ds1.properties";
	private String props2 = "resources/db/ds2.properties";

	private String VIRTUAL_MODEL_NAME = "PartsVirtual.xmi";
	private static final String VIRTUAL_TABLE_NAME = "OnHand";
	private static final String PROCEDURE_NAME = "getOnHandByQuantity";


	private static final String VDB_NAME = "MyFirstVDB";
	private static final String VDB_FILE_NAME = VDB_NAME + ".vdb";
	
	private static String TESTSQL_1 = "select * from \"partssupModel1\".\"PARTS\"";
	private static String TESTSQL_2 = "SELECT * FROM PartsVirtual.OnHand";
	private static final String TESTSQL_3 = "SELECT * FROM PartsVirtual.OnHand WHERE QUANTITY > 200"; 

private static final String TESTSQL_4 = "SELECT " + "O.SUPPLIER_NAME, " + "O.PART_ID, " + "P.PART_NAME " +

"FROM " + "\"partssupModel1\".PARTS AS P, " + "PartsVirtual.OnHand AS O " + "WHERE "
+ "(P.PART_ID = O.PART_ID) and " + "(O.SUPPLIER_NAME LIKE '%la%') " + "ORDER BY PART_NAME"; // it
	
	private static final String PROCEDURE_SQL = "CREATE VIRTUAL PROCEDURE\n" + "BEGIN\n\t"
			+ "SELECT * FROM PartsVirtual.OnHand " + "WHERE PartsVirtual.OnHand.QUANTITY = "
			+ "PartsVirtual.getOnHandByQuantity.qtyIn;\n" + "END";
	
	
	/**
	 * Create new Teiid Model Project
	 */
	@Test
	public void createProject(){
		int currentPage = 0;//currentPage of wizard must be set to 0
		new ModelProjectWizard(currentPage).create(PROJECT_NAME, true);
	}
	
	/**
	 * Create connection profiles to HSQL databases
	 */
	@Test
	public void createConnProfiles(){
		teiidBot.createHsqlProfile(props1, jdbcProfile, true, true);
		teiidBot.createHsqlProfile(props2, jdbcProfile2, false, true);
	}
	
	/**
	 * Create data sources for connection profiles
	 */
	@Test
	public void createSources(){
		//datasource ds1
		importFromHsql(jdbcProfile, SOURCE_MODEL_1);
		
		//datasource ds2
		importFromHsql(jdbcProfile2, SOURCE_MODEL_2);//import the same tables
	}
	
	/**
	 * Preview data from model
	 */
	@Test
	public void previewData(){
		//wait until project is saved
		new WaitWhile(new IsInProgress(), TimePeriod.LONG);
		new GuidesView().previewData(true, PROJECT_NAME, SOURCE_MODEL_1, "PARTS");
		
		SQLResult result = DatabaseDevelopmentPerspective.getInstance().getSqlResultsView().getByOperation(TESTSQL_1);
		assertEquals(SQLResult.STATUS_SUCCEEDED, result.getStatus());
		
		new WaitWhile(new IsInProgress(), TimePeriod.NORMAL);
		TeiidPerspective.getInstance().open();
	}
	
	/**
	 * Create view model
	 */
	@Test
	public void createViewModel() {
		CreateMetadataModel newModel = new CreateMetadataModel();
		newModel.setLocation(PROJECT_NAME);
		newModel.setName(VIRTUAL_MODEL_NAME );
		newModel.setClass(CreateMetadataModel.ModelClass.RELATIONAL);
		newModel.setType(CreateMetadataModel.ModelType.VIEW);
		newModel.execute();

		saveAll();
		//parts virtual have been modified - save changes
		closeActiveShell();
	}
	
	/**
	 * Create transformation
	 */
	@Test
	public void createTransformation() {
		ModelExplorerView modelView = TeiidPerspective.getInstance().getModelExplorerView();
		modelView.newBaseTable(PROJECT_NAME, VIRTUAL_MODEL_NAME, VIRTUAL_TABLE_NAME, false);
		modelView.openTransformationDiagram(PROJECT_NAME, VIRTUAL_MODEL_NAME, VIRTUAL_TABLE_NAME);
		modelView.addTransformationSource(PROJECT_NAME, SOURCE_MODEL_1, "SUPPLIER");
		modelView.addTransformationSource(PROJECT_NAME, SOURCE_MODEL_2, "SUPPLIER_PARTS");

		ModelEditor editor = new ModelEditor(VIRTUAL_MODEL_NAME);
		editor.show();
		editor.showTransformation();

		CriteriaBuilder criteriaBuilder = editor.criteriaBuilder();
		criteriaBuilder.selectRightAttribute(SOURCE_MODEL_1.substring(0, SOURCE_MODEL_1.indexOf('.')).concat(".SUPPLIER"), "SUPPLIER_ID");
		criteriaBuilder.selectLeftAttribute(SOURCE_MODEL_2.substring(0, SOURCE_MODEL_2.indexOf('.')).concat(".SUPPLIER_PARTS"), "SUPPLIER_ID");
		criteriaBuilder.apply();
		criteriaBuilder.finish();

		editor.save();
		closeActiveShell();
	}

	/**
	 * Create VDB
	 */
	@Test
	public void createVDB() {
		CreateVDB createVDB = new CreateVDB();
		createVDB.setFolder(PROJECT_NAME);
		createVDB.setName(VDB_NAME);
		createVDB.execute(true);

		VDBEditor editor = VDBEditor.getInstance(VDB_FILE_NAME);
		editor.show();
		editor.addModel(PROJECT_NAME, VIRTUAL_MODEL_NAME);
		editor.save();
	}
	
	/**
	 * Deploy, execute VDB
	 */
	@Test
	public void executeVDB() {
		//switch to teiid designer perspective
		
		ModelExplorer modelExplorer = new ModelExplorer();
		modelExplorer.open();
		VDB vdb = modelExplorer.getModelProject(PROJECT_NAME).getVDB(VDB_FILE_NAME);
		vdb.deployVDB();
		vdb.executeVDB(true);
		
		closeActiveShell();
	}
	
	/**
	 * Execute test queries in SQL Scrapbook
	 */
	@Test
	public void executeSqlQueries() {
		SQLScrapbookEditor editor = new SQLScrapbookEditor("SQL Scrapbook0");
		editor.show();
		editor.setDatabase(VDB_NAME);

		// TESTSQL_1
		editor.setText(TESTSQL_1);
		editor.executeAll();

		SQLResult result = DatabaseDevelopmentPerspective.getInstance().getSqlResultsView()
				.getByOperation(TESTSQL_1);
		assertEquals(SQLResult.STATUS_SUCCEEDED, result.getStatus());

		// TESTSQL_2
		editor.show();
		editor.setText(TESTSQL_2);
		editor.executeAll();

		result = DatabaseDevelopmentPerspective.getInstance().getSqlResultsView().getByOperation(TESTSQL_2);
		assertEquals(SQLResult.STATUS_SUCCEEDED, result.getStatus());

		// TESTSQL_3
		editor.show();
		editor.setText(TESTSQL_3);
		editor.executeAll();

		result = DatabaseDevelopmentPerspective.getInstance().getSqlResultsView().getByOperation(TESTSQL_3);
		assertEquals(SQLResult.STATUS_SUCCEEDED, result.getStatus());

		// TESTSQL_4
		editor.show();
		editor.setText(TESTSQL_4);
		editor.executeAll();

		result = DatabaseDevelopmentPerspective.getInstance().getSqlResultsView().getByOperation(TESTSQL_4);
		assertEquals(SQLResult.STATUS_SUCCEEDED, result.getStatus());
	}
		
	/**
	 * Import tables from HSQL database
	 * @param connProfile name of connection profile (e.g. HSQLDB Profile)
	 * @param modelName name of model (e.g. partssupplier.xmi)
	 */
	public void importFromHsql(String connProfile, String modelName) {
		ImportJDBCDatabaseWizard wizard = new ImportJDBCDatabaseWizard();
		wizard.setConnectionProfile(connProfile);
		wizard.setProjectName(PROJECT_NAME);
		wizard.setModelName(modelName);
		// add all tables
		wizard.addItem("PUBLIC/PUBLIC/TABLE/PARTS");
		wizard.addItem("PUBLIC/PUBLIC/TABLE/SHIP_VIA");
		wizard.addItem("PUBLIC/PUBLIC/TABLE/STATUS");
		wizard.addItem("PUBLIC/PUBLIC/TABLE/SUPPLIER");
		wizard.addItem("PUBLIC/PUBLIC/TABLE/SUPPLIER_PARTS");

		wizard.execute(true);
	}
	
	/**
	 * Check if model contains object on given path
	 * @param path
	 */
	private void checkResource(String... path) {
		Bot.get().sleep(500);
		ModelProject modelproject = teiidBot.modelExplorer().getModelProject(PROJECT_NAME);
		assertTrue(Arrays.toString(path) + " not created!", modelproject.containsItem(path));
	}
	
	/**
	 * Save all
	 */
	private void saveAll(){
		new ShellMenu("File", "Save All").select();
	}
	
	/**
	 * Confirm active shell, if appears
	 */
	private void closeActiveShell(){
		try {
			Bot.get().activeShell().bot().button("Yes").click();
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}
	
}
