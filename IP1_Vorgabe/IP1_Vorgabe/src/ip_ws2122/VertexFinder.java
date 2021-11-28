package ip_ws2122;

import java.util.ArrayList;
import java.util.List;

public class VertexFinder {
	public List<Integer> allVertices= new ArrayList<Integer>();
	public int vLength = 0;
	RasterImage image;
	
	public VertexFinder(RasterImage image) {
		this.image = image;
	}
	
	public void addVertex(Vertex vertex) {
		int index = Helper.coordsToIndex(vertex, image.width);
		this.allVertices.add(index);
	}
	
	public void findAllVertices() {
		Vertex vertex;
		for(int x=0; x <this.image.width; x++) {
			for(int y=0; y < this.image.height; y++) {
				vertex = new Vertex(x,y);
				if(vertex.checkIfEdge(image)) {
					
				}
				
			}
		}
	}
	
	
	
	
	
	
}
