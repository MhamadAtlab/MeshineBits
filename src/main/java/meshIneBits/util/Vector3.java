package meshIneBits.util;

/**
 * Vector3 represents a point in 3D space.
 */
public class Vector3 {
	public double x, y, z;

	public Vector3() {
		// TODO Auto-generated constructor stub
	}

	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void addToSelf(Vector3 v) {
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public Vector3 cross(Vector3 v) {
		return new Vector3((y * v.z) - (z * v.y), (z * v.x) - (x * v.z), (x * v.y) - (y * v.x));
	}

	public Vector3 div(double f) {
		return new Vector3(x / f, y / f, z / f);
	}

	/**
	 * Return the scalar of two vector
	 */
	public double dot(Vector3 v) {
		return (x * v.x) + (y * v.y) + (z * v.z);
	}

	public Vector3 normal() {
		return div(vSize());
	}

	public Vector3 sub(Vector3 v) {
		return new Vector3(x - v.x, y - v.y, z - v.z);
	}

	@Override
	public String toString() {
		return x + "," + y + "," + z;
	}

	/**
	 * Return the size (length) of the vector
	 */
	public double vSize() {
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}

	/**
	 * Return the squared size (length) of the vector
	 */
	public double vSize2() {
		return (x * x) + (y * y) + (z * z);
	}
}
