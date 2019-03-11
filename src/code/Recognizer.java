package code;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.opencv.core.Mat;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.Crop;
import clarifai2.dto.model.DemographicsModel;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Prediction;
import clarifai2.dto.prediction.Region;
import javafx.scene.image.WritableImage;

public class Recognizer {
	private final String apiKey = "8009a8c789224909b06467b96b7bbcb9";
	private ClarifaiClient CB;
	private Mat mat;
	
	public Recognizer(Mat _mat) throws IOException {
		CB = new ClarifaiBuilder(apiKey).buildSync();
		mat = _mat;
		getRequest();
	}
	
	public Results getRequest() throws IOException {
		
		ClarifaiResponse<List<ClarifaiOutput<Region>>> CR = CB.getDefaultModels().demographicsModel().predict()
		
		.executeSync();
		
		DemographicsModel generalModel = CB.getDefaultModels().demographicsModel();
		PredictRequest<Region> request = generalModel.predict().withInputs(ClarifaiInput.forImage(ImageUtils.mat2Bytes(mat)));
		List<ClarifaiOutput<Region>> result = request.executeSync().get();
		ClarifaiOutput<Region> r = result.get(0);
		
			ClarifaiOutput<Region> region = result.get(0);
			List<Region> lr = region.data();
			List<Concept> WHAT = lr.get(0).multiculturalAppearances();
			List<Concept> WHO = lr.get(0).genderAppearances();
			Results retVal;
			for(Concept c: WHAT) {
				c.
			}
			System.out.println(WHAT);
			System.out.println(WHO);
		
		return retVal;
	}
	public class Results {
		
	}
}
