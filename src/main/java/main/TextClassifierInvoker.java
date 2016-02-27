package main;

import com.datumbox.applications.nlp.TextClassifier;
import com.datumbox.common.dataobjects.Dataset;
import com.datumbox.common.dataobjects.Record;
import com.datumbox.common.persistentstorage.ConfigurationFactory;
import com.datumbox.common.persistentstorage.interfaces.DatabaseConfiguration;
import com.datumbox.framework.machinelearning.classification.BinarizedNaiveBayes;
import com.datumbox.framework.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.machinelearning.ensemblelearning.BayesianEnsembleMethod;
import com.datumbox.framework.machinelearning.featureselection.categorical.ChisquareSelect;
import com.datumbox.framework.utilities.text.extractors.NgramsExtractor;

import java.util.UUID;

/**
 * Created by tomas on 27-02-16.
 */
public class TextClassifierInvoker {

    public static TextClassifier apply(String name, Record[] records) throws InterruptedException {
        DatabaseConfiguration dbConf = ConfigurationFactory.INMEMORY.getConfiguration();
        //Setup Training Parameters
        //-------------------------
        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();

        //Classifier configuration
        trainingParameters.setMLmodelClass(BinarizedNaiveBayes.class);
        trainingParameters.setMLmodelTrainingParameters(new BinarizedNaiveBayes.TrainingParameters());

        //Set data transfomation configuration
        trainingParameters.setDataTransformerClass(null);
        trainingParameters.setDataTransformerTrainingParameters(null);

        //Set feature selection configuration
        trainingParameters.setFeatureSelectionClass(ChisquareSelect.class);
        trainingParameters.setFeatureSelectionTrainingParameters(new ChisquareSelect.TrainingParameters());

        //Set text extraction configuration
        trainingParameters.setTextExtractorClass(NgramsExtractor.class);
        trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters());


        TextClassifier classifier = new TextClassifier(name, dbConf);

//        logger.info("Fitting TextClassifier");

        Dataset dataset = new Dataset(dbConf);

        for (Record record: records) {
            dataset.add(record);
        }

        classifier.fit(dataset.copy(), trainingParameters);

        return classifier;
    }

}