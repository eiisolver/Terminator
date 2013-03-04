package commander;

public interface TileVisitor {
	/**
	 * Visits the tile with the given coordinates
	 * @param row
	 * @param col
	 * @return false if we should stop
	 */
	public boolean visit(Tile t);

}
