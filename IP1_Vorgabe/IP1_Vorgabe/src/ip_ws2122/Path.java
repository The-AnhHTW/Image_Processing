package ip_ws2122;

import java.util.List;

public class Path {
	private List<int[]> geschlossenPfad;
	private boolean innere;
	private boolean aussere;

	public Path (List<int[]> path, boolean innere, boolean aussere) {
		this.geschlossenPfad = path;
		this.aussere = aussere;
		this.innere = innere;
	}
	
	
	public List<int[]> getEckPunkte() {
		return geschlossenPfad;
	}

	public void setEckPunkte(List<int[]> geschlossenPfad) {
		this.geschlossenPfad = geschlossenPfad;
	}

	public boolean isInnere() {
		return innere;
	}

	public void setInnere(boolean innere) {
		this.innere = innere;
	}

	public boolean isAussere() {
		return aussere;
	}

	public void setAussere(boolean aussere) {
		this.aussere = aussere;
	}

}
