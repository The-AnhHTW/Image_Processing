package ip_ws2122;

public class Vektor2d {
	int x;
	int y;

	public Vektor2d(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public static int crossProduct(Vektor2d a, Vektor2d b) {
		return (a.x * b.y) - (a.y*b.x);
	}
	
	
}
