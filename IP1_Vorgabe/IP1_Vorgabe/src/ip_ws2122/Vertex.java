package ip_ws2122;

public class Vertex implements Coordinate2d {
	public int x;
	public int y;
	
//	public Vertex(int i, int j) {
//		// TODO Auto-generated constructor stub
//	}

	public Vertex (int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public VertexNeighbours neighbourPixelCoords(RasterImage img) {
		VertexNeighbours neighbours = new VertexNeighbours();
//		Borders borderCheck = this.checkIfBorder();
		
		
		
		return neighbours;
		
		
	}
	
	
	public boolean checkIfEdge(RasterImage img) {
		
		return true;
	}
	
	public Borders checkIfBorder(RasterImage img) {
		Borders borders = new Borders();
		return null;
		
//		borders.top = 
	}
	
	
}
