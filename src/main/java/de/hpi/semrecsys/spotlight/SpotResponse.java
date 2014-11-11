package de.hpi.semrecsys.spotlight;

import de.hpi.semrecsys.spotlight.SpotlightConnector.EndpointType;

public class SpotResponse extends SpotlightResponse {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getJsonString());
		String result = "Response [" + "\n\t text = " + getText() + ", ";
		builder.append(result);
		builder.append("]\n\t surfaceForms = [");
		for (SurfaceForm surfaceForm : getSurfaceForms()) {
			SpotSurfaceForm spotSurfaceForm = (SpotSurfaceForm) surfaceForm;
			builder.append(spotSurfaceForm + "\n");
		}
		builder.append("]\n]");

		return builder.toString();
	}

	@Override
	public EndpointType getType() {
		return EndpointType.SPOT;
	}

	public static class SpotSurfaceForm extends SurfaceForm {

		@Override
		public String toString() {
			String result = "Name = " + getName() + ", Offset = " + getOffset();
			return result;
		}
	}

}
