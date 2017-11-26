package instantiator.pairing.data;

public class TouchEventData {

  public TouchEventData() {}

  public TouchEventData(int grid_x, int grid_y, int colour, int radius) {
    this.grid_x = grid_x;
    this.grid_y = grid_y;
    this.colour = colour;
    this.radius = radius;
  }

  public int grid_x;
  public int grid_y;
  public int colour;
  public int radius;

}
