package ip_ws2122;

public class Vektor2d {
	
	boolean pivot= false;
	int indexOfPivot = 0;
	
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	int x;
	int y;

	public Vektor2d(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vektor2d(int x, int y, boolean pivot, int indexOfPivot) {
		this.x = x;
		this.y = y;
		this.pivot = pivot;
		this.indexOfPivot = indexOfPivot;
	}
	
	
	
	public static int crossProduct(Vektor2d a, Vektor2d b) {
		return ((a.x * b.y) - (a.y*b.x));
	}
	
	
}
