package de.hpi.semrecsys.main;

import static org.junit.Assert.assertTrue;
import de.hpi.semrecsys.config.SemRecSysConfigurator;
import de.hpi.semrecsys.config.SemRecSysConfigurator.Customer;
import de.hpi.semrecsys.populator.Populator;
import de.hpi.semrecsys.populator.Populator.PopulationOption;

/***
 * Class used for the population of the virtuoso data server
 * @author Michael Wolowyk
 *
 */
public class PopulatorMain {

	static Customer customer = Customer.melovely;
	static SemRecSysConfigurator configurator = SemRecSysConfigurator.getDefaultConfigurator(customer);

	static Populator populator = new Populator(configurator);

	private static int numberOfProducts = 1000;


	public static void main(String[] args) {
		PopulationOption[] options = { PopulationOption.meta, PopulationOption.attribute_sim, PopulationOption.products, PopulationOption.entity_sim};
		execute(true, options);
	}

	/***
	 * Executes population of the Virtuoso data server
	 * @param options configuration options
	 */
	public static void execute(boolean clean, PopulationOption... options) {
        long start = System.currentTimeMillis();
		populator.populate(numberOfProducts, clean, options);
        long end = System.currentTimeMillis();
        System.out.println("Time : " + String.valueOf(end - start) + " ms");
	}

	/**
	 * Deletes graphs from the Virtuoso Datasource
	 * @param graphs graphs to delete
	 */
	public static void cleanGraphs(String[] graphs) {
		for (String graphName : graphs) {
			populator.cleanGraph(graphName);
			assertTrue(0 == populator.getGraphSize(graphName));
		}
	}

    public static void cleanGraph(String graphName){
        populator.cleanGraph(graphName);
    }

    public static String getGraphs(){
        return populator.getGraphSizes();
    }

}
