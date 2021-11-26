package ip_ws2122;

public class Path {
	private int[][] geschlossenPfad;
	private boolean innere;
	private boolean aussere;

	public int[][] getEckPunkte() {
		return geschlossenPfad;
	}

	public void setEckPunkte(int[][] geschlossenPfad) {
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
