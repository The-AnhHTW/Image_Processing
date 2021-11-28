package ip_ws2122;

public class Helper {

	public static int coordsToIndex(Coordinate2d coordinate, int width) {
		return coordinate.y * width + coordinate.x;
	}
	
	public static Coordinate2d indexToCord(int index, int width) {
		return new Vertex(index/width, index%width);
	}
	
}
