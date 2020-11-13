import java.lang.Math.*;


/**
 * Quaternion matrix
 */
class Qua4 {
	float x, y, z, w;
	
	Qua4() {
		x = 0;
		y = 0;
		z = 0;
		w = 0;
	}
	
	// *Attention 4th parameter sets real
	Qua4(float fx, float fy, float fz, float fw) {
		x = fx;
		y = fy;
		z = fz;
		w = fw;
	}
	
	void print(String strName) {
		System.out.printf("%s\t%8.3f : %8.3f : %8.3f : %8.3f\n", strName, x, y, z, w);
	}
	
	void print() {
		System.out.printf("%8.3f : %8.3f : %8.3f : %8.3f\n", x, y, z, w);
	}
	
	
	Qua4 plus(Qua4 v) {
		return new Qua4(x + v.x, y + v.y, z + v.z, w + v.w);
	}
	
	float dmul(Qua4 v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	Qua4 xmul(Qua4 v) {
		return new Qua4(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x, 0);
	}
	
	static Qua4 mult(Qua4 a, Qua4 b) {
		Qua4 v = new Qua4();
		v.w = a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z;
		v.x = a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y;
		v.y = a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x;
		v.z = a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w;
		
	 	return v;
	}
	
	/*
	 @param vp  vector of point
	 @param ax..az  vector of normalize axis
	 @param rad radian of rotation 
	*/
	static Qua4 rot(Qua4 vp, float ax, float ay, float az, float rad) {
		float hrad = rad / 2;
		Qua4 q = new Qua4(ax * (float)Math.sin(hrad), ay * (float)Math.sin(hrad), az * (float)Math.sin(hrad), (float)Math.cos(hrad));
		//Qua4 r = new Qua4(-va.x * (float)Math.sin(hrad), -va.y * (float)Math.sin(hrad), -va.z * (float)Math.sin(hrad), (float)Math.cos(hrad));
		
		Qua4 p = Qua4.mult(q, vp);
		
		//p = Qua4.mult(p, r);
		//p.print("PxR");
		
		return p;
	}
	
	
	void toMatrix(float[] fb)
	{
		float qw, qx, qy, qz;
		float x2, y2, z2;
		float xy, yz, zx;
		float wx, wy, wz;

		qw = w; qx = x; qy = y; qz = z;

		x2 = 2.0f * qx * qx;
		y2 = 2.0f * qy * qy;
		z2 = 2.0f * qz * qz;

		xy = 2.0f * qx * qy;
		yz = 2.0f * qy * qz;
		zx = 2.0f * qz * qx;
		
		wx = 2.0f * qw * qx;
		wy = 2.0f * qw * qy;
		wz = 2.0f * qw * qz;

		fb[0] = 1.0f - y2 - z2;
		fb[1] = xy + wz;
		fb[2] = zx - wy;
		fb[3] = 0.0f;

		fb[4] = xy - wz;
		fb[5] = 1.0f - z2 - x2;
		fb[6] = yz + wx;
		fb[7] = 0.0f;

		fb[8] = zx + wy;
		fb[9] = yz - wx;
		fb[10] = 1.0f - x2 - y2;
		fb[11] = 0.0f;

		fb[12] = fb[13] = fb[14] = 0.0f;
		fb[15] = 1.0f;
	}

	void set(Qua4 src) {
		x = src.x;
		y = src.y;
		z = src.z;
		w = src.w;
	}

}
