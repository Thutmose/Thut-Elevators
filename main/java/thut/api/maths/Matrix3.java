package thut.api.maths;

import static net.minecraftforge.common.util.ForgeDirection.*;

import java.util.ArrayList;
import java.util.List;

import thut.api.entity.IMultiBox;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;


public class Matrix3 {
	
	public static boolean collisionDamage = false;
	
	public Vector3[] Rows = new Vector3[3];
	int size = 3;

	public Matrix3() {
		Rows[0] = new Vector3();
		Rows[1] = new Vector3();
		Rows[2] = new Vector3();
	}

	public Matrix3(double[] a, double[] b, double[] c) {
		Rows[0] = new Vector3(a[0], a[1], a[2]);
		Rows[1] = new Vector3(b[0], b[1], b[2]);
		Rows[2] = new Vector3(c[0], c[1], c[2]);
	}

	public Matrix3(Vector3 a, Vector3 b, Vector3 c) {
		Rows[0] = a.Copy();
		Rows[1] = b.Copy();
		Rows[2] = c.Copy();
	}

	public Matrix3(Vector3 a, Vector3 b) {
		this(a, b, new Vector3(0, 0, 0));
	}

	public Matrix3(double d, double e, double f) {
		this();
		Rows[1].set(d, e, f);
	}

	public Matrix3(double d, double e, double f, Vector3 vector3) {
		this(d, e, f);
		Rows[2] = vector3;
	}

	public Vector3 get(int i) {
		assert (i < 3);
		return Rows[i];
	}

	public Vector3 boxMin() {
		return Rows[0];
	}

	public Vector3 boxMax() {
		return Rows[1];
	}

	public Vector3 boxRotation() {
		return Rows[2];
	}

	public double get(int i, int j) {
		assert (i < 3);
		return Rows[i].get(j);
	}

	public double boxZLength() {
		return Math.abs(get(1, 2) - get(0, 2));
	}

	public double boxYLength() {
		return Math.abs(get(1, 1) - get(0, 1));
	}

	public double boxXLength() {
		return Math.abs(get(1, 0) - get(0, 0));
	}

	/**
	 * 0 = min, min, min;
	 * 1 = max, max, max;
	 * 2 = min, min, max;
	 * 3 = min, max, min;
	 * 4 = max, min, min;
	 * 5 = min, max, max;
	 * 6 = max, min, max;
	 * 7 = max. max, min;
	 * @return
	 */
	public Vector3[] corners() {
		Vector3[] corners = new Vector3[8];

		corners[0] = boxMin();
		corners[1] = boxMax();

		corners[2] = new Vector3(boxMin().x, boxMin().y, boxMax().z);
		corners[3] = new Vector3(boxMin().x, boxMax().y, boxMin().z);
		corners[4] = new Vector3(boxMax().x, boxMin().y, boxMin().z);

		corners[5] = new Vector3(boxMin().x, boxMax().y, boxMax().z);
		corners[6] = new Vector3(boxMax().x, boxMin().y, boxMax().z);
		corners[7] = new Vector3(boxMax().x, boxMax().y, boxMin().z);

		if (!boxRotation().isEmpty()) {
			Vector3.rotateAboutAngles(corners, boxRotation().y,
					boxRotation().z);
		}

		return corners;
	}

	public Matrix3 addOffset(Vector3 pushOffset) {
		Matrix3 ret = this.copy();
		ret.Rows[0] = ret.Rows[0].add(pushOffset);
		ret.Rows[1] = ret.Rows[1].add(pushOffset);
		return ret;
	}

	public Matrix3 copy() {
		Matrix3 ret = new Matrix3(Rows[0].Copy(), Rows[1].Copy(), Rows[2].Copy());
		return ret;
	}

	public Matrix3 set(int i, Vector3 j) {
		assert (i < 3);
		Rows[i] = j;
		return this;
	}

	public void set(int i, int j, double k) {
		Rows[i].set(j, k);
	}

	public double[][] toArray() {
		return new double[][] { 
				{Rows[0].x,Rows[0].y,Rows[0].z}, 
				{Rows[1].x,Rows[1].y,Rows[1].z},
				{Rows[2].x,Rows[2].y,Rows[2].z} 
				};
	}

	public Matrix3 addToRows(Vector3 vec) {
		return new Matrix3(Rows[0].add(vec), Rows[1].add(vec),
				Rows[2].add(vec));
	}

	public String toString() {
		String eol = System.getProperty("line.separator");
		return eol + "0: " + Rows[0].toString() + eol + "1: "
				+ Rows[1].toString() + eol + "2 : " + Rows[2].toString();
	}

	/**
	 * Multiplies MatrixA by MatrixB in the form AB.
	 * 
	 * @param MatrixA
	 * @param MatrixB
	 * @return
	 */
	public static Matrix3 matrixMatrixMult(Matrix3 MatrixA, Matrix3 MatrixB) {
		Matrix3 MatrixC = new Matrix3();
		MatrixB = matrixTranspose(MatrixB);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				MatrixC.set(i, j, Vector3.vectorDot(MatrixA.get(i), MatrixB.get(j)));
			}
		}
		return MatrixC;
	}

	/**
	 * Transposes the given Matrix
	 * 
	 * @param Matrix
	 * @return
	 */
	public static Matrix3 matrixTranspose(Matrix3 Matrix) {
		Matrix3 MatrixT = new Matrix3();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				MatrixT.set(i, j, Matrix.get(j, i));
			}
		}
		return MatrixT;
	}

	/**
	 * Computes the Inverse of the matrix
	 * 
	 * @param Matrix
	 * @return
	 */
	public static Matrix3 matrixInverse(Matrix3 Matrix) {
		Matrix3 Inverse = new Matrix3();
		double det = matrixDet(Matrix);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				Inverse.set(i, j, Math.pow(-1, i + j)
						* matrixDet(matrixMinor(Matrix, i, j)) / det);
			}
		}
		Inverse = matrixTranspose(Inverse);
		return Inverse;
	}

	/**
	 * Computes the Determinant of the given matrix, Matrix must be square.
	 * 
	 * @param Matrix
	 * @return
	 */
	public static double matrixDet(Matrix3 Matrix) {
		double det = 0;
		int n = Matrix.size;
		if (n == 2) {
			det = Matrix.get(0, 0) * Matrix.get(1, 1) - Matrix.get(1, 0)
					* Matrix.get(0, 1);
		} else {
			for (int i = 0; i < n; i++) {
				det += Math.pow(-1, i) * Matrix.get(0, i)
						* matrixDet(matrixMinor(Matrix, 0, i));
			}
		}
		return det;
	}

	/**
	 * Computes the minor matrix formed from removal of the ith row and jth
	 * column of matrix.
	 * 
	 * @param Matrix
	 * @param i
	 * @param j
	 * @return
	 */
	public static Matrix3 matrixMinor(Matrix3 input, int i, int j) {
		double[][] Matrix = input.toArray();
		int n = Matrix.length;
		int m = Matrix[0].length;
		Double[][] TempMinor = new Double[m - 1][n - 1];
		List<ArrayList<Double>> row = new ArrayList<ArrayList<Double>>();
		for (int k = 0; k < n; k++) {
			if (k != i) {
				row.add(new ArrayList<Double>());
				for (int l = 0; l < m; l++) {
					if (l != j) {
						row.get(k - (k > i ? 1 : 0)).add(Matrix[k][l]);
					}
				}
			}
		}
		for (int k = 0; k < n - 1; k++) {
			TempMinor[k] = row.get(k).toArray(new Double[0]);
		}
		Matrix3 Minor = new Matrix3();
		Minor.size = n - 1;
		for (int k = 0; k < n - 1; k++) {
			for (int l = 0; l < m - 1; l++) {
				Minor.set(k, l, TempMinor[k][l]);
			}
		}
		return Minor;
	}

	/**
	 * Adds MatrixA and MatrixB
	 * 
	 * @param MatrixA
	 * @param MatrixB
	 * @return
	 */
	public static Matrix3 matrixAddition(Matrix3 MatrixA, Matrix3 MatrixB) {
		Matrix3 MatrixC = new Matrix3();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				MatrixC.set(i, i, MatrixA.get(i, j) + MatrixB.get(i, j));
			}
		}
		return MatrixC;
	}

	/**
	 * Subtracts MatrixB from MatrixA
	 * 
	 * @param MatrixA
	 * @param MatrixB
	 * @return
	 */
	public static Matrix3 matrixSubtraction(Matrix3 MatrixA, Matrix3 MatrixB) {
		Matrix3 MatrixC = new Matrix3();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				MatrixC.set(i, i, MatrixA.get(i, j) - MatrixB.get(i, j));
			}
		}
		return MatrixC;
	}

	/**
	 * Multiplies the Matrix by the scalar
	 * 
	 * @param Matrix
	 * @param scalar
	 * @return
	 */
	public static Matrix3 matrixScalarMuli(Matrix3 Matrix, double scalar) {
		Matrix3 ret = new Matrix3();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				ret.set(i, j, Matrix.get(i, j) * scalar);
			}
		}
		return ret;
	}

	public boolean pushOutOfBox(Entity pusher, Entity e, Vector3 offset) {
		boolean ret = false;
		ret = doEntityCollision(pusher, e, offset);
		return ret;
	}

	public boolean doEntityCollision(Entity pusher, Entity e, Vector3 offset) {
		return doEntityCollision(pusher, e, offset, new Vector3());
	}

	public boolean doEntityCollision(Entity pusher, Entity e,
			Vector3 offset, Vector3 entity) {
		return doCollision(new Vector3(pusher), new Vector3(pusher.motionX,
				pusher.motionY, pusher.motionZ), e, offset, entity);
	}

	public Vector3 doTileCollision(IBlockAccess world, Entity e, Vector3 offset, Vector3 diffs, boolean move) {
		if(!(e instanceof IMultiBox))
			return diffs;
		boolean ret = false;
		Vector3 ent = new Vector3(e);
		Vector3 temp = new Vector3();
		Vector3[] corners = corners();
		boolean[] collides = new boolean[8];
		int count = 0;
		for(int i = 0; i<8; i++)
		{
			Vector3 v = corners[i];
			temp.set(v.add(ent).add(offset));
			corners[i] = temp.Copy();
			if(!temp.clearOfBlocks(world))
			{
				collides[i] = true;
				ret = true;
				count++;
			}
			if(i%2==0)
			{
				temp = temp.add(0, -0.1, 0);

				if(!temp.clearOfBlocks(world))
				{
					collides[i] = true;
					ret = true;
					count++;
				}
				
			}
		}
		if(!ret)
		{
			return diffs;
		}
		List<Vector3> cor = new ArrayList();
		for(Vector3 v: corners)
		{
			cor.add(v.Copy());
		}
		boolean ground = false;
		boolean jumping = false;
		
		Vector3 dir = new Vector3();
		//TODO special cases for jumping
		//TODO check areas between for large pokemon
		boolean wall = false;
		//Push out of walls
		if((collides[0] && collides[3])||(collides[2] && collides[5])||(collides[4] && collides[7])||(collides[6] && collides[1]))
		{
			Vector3 boxMid = Vector3.findMidPoint(cor);
			List<Vector3> collisions = new ArrayList();
			for(int i = 0; i<8; i++)
			{
				if(collides[i])
					collisions.add(corners[i]);
			}
			Vector3 mid = Vector3.findMidPoint(collisions);
			dir = boxMid.subtract(mid);
			if(move)
			{
				diffs.y = 0;
				e.setPosition(e.posX+dir.x, e.posY, e.posZ+dir.z);
				if(!jumping)
				{
					e.motionX = dir.x/10;
					e.motionZ = dir.x/10;
					e.motionY = 0;
				}
			}
			wall = true;
		}
		//Push out of ground below
		if(!wall && (collides[0]||collides[2]||collides[4]||collides[6]) && (e.motionY<=0))
		{
			double max = 0;
			int j = 0;
			Vector3 v;
			Block b;
			for(int i = 0; i<4; i++)
			{
				int index = 2*i;
				v = corners[index];
				b = v.getBlock(world);
				if(v.intY()+b.getBlockBoundsMaxY()>max)
				{
					max = v.intY()+b.getBlockBoundsMaxY();
					j = index;
				}
			}
			v = corners[j].Copy();
			b = v.getBlock(world);
			double up = b.getMaterial().isSolid()?b.getBlockBoundsMaxY():0;
			//down
			ground = true;
			if(move)
				e.setPosition(e.posX, v.intY()+up, e.posZ);
			else
				dir.y = up;
			diffs.y = 0;
			e.motionY = 0;
		}
		
		//Push out of cieling above
		if(!wall && (collides[1]||collides[3]||collides[5]||collides[7]))
		{
			double min = 255;
			int j = 0;
			Vector3 v;
			Block b;
			for(int i = 0; i<4; i++)
			{
				int index = 2*i + 1;
				v = corners[index];
				b = v.getBlock(world);
				if(v.intY()+b.getBlockBoundsMinY()<min)
				{
					min = v.intY()+b.getBlockBoundsMinY();
					j = index;
				}
			}
			v = corners[j].Copy();
			b = v.getBlock(world);
			ground = false;
			double dy = v.intY()+b.getBlockBoundsMinY() - e.posY;
			if(dy > 0.01)
				if(move)
					e.setPosition(e.posX, e.posY - dy - 0.011, e.posZ);
			diffs.y = 0;//Math.max(y - e.posY, 0.001);
			e.motionY = 0;
		}
		e.onGround = ground;
		return diffs;
	}

	public boolean doTileCollision(IBlockAccess world, Vector3 location, Entity e, Vector3 offset, Vector3 diffs, boolean move) {
		if(!(e instanceof IMultiBox))
			return false;
		boolean ret = false;
		Vector3 ent = location;
		Vector3 temp = new Vector3();
		Vector3[] corners = corners();
		boolean[] collides = new boolean[8];
		
		for(int i = 0; i<8; i++)
		{
			Vector3 v = corners[i];
			temp.set(v.add(ent).add(offset));
			corners[i] = temp.Copy();
			if(!temp.clearOfBlocks(world))
			{
				collides[i] = true;
				ret = true;
			}
			if(i%2==0)
			{
				temp = temp.add(0, -0.1, 0);

				if(!temp.clearOfBlocks(world))
				{
					collides[i] = true;
					ret = true;
				}
				
			}
		}
		if(!ret)
		{
			return false;
		}
		List<Vector3> cor = new ArrayList();
		for(Vector3 v: corners)
		{
			cor.add(v.Copy());
		}
		boolean ground = false;
		boolean jumping = false;

		Vector3 dir = new Vector3();

//		//Push out of cieling above
		if((collides[1]&&collides[3]&&collides[5]&&collides[7]) && (e.motionY>=0))
		{
			return true;
		}
		//Push out of walls
		if((collides[0] && collides[3])||(collides[2] && collides[5])||(collides[4] && collides[7])||(collides[6] && collides[1]))
		{
			return true;
		}
		return false;
	}

	public boolean isInMaterial(IBlockAccess world, Vector3 location, Entity e, Vector3 offset, Material m) {
		if(!(e instanceof IMultiBox))
			return false;
		boolean ret = false;
		Vector3 ent = location;
		Vector3 temp = new Vector3();
		Vector3[] corners = corners();
		
		for(int i = 0; i<8; i++)
		{
			Vector3 v = corners[i];
			temp.set(v.add(ent).add(offset));
			if(temp.getBlockMaterial(world)==m)
			{
				return true;
			}
			if(i%2==0)
			{
				temp = temp.add(0, 0.01, 0);
				if(temp.getBlockMaterial(world)==m)
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean doCollision(Vector3 push, Vector3 v, Entity e,
			Vector3 offset, Vector3 entity) {
		boolean ret = false;
		if(e==null)
			return false;
		
		offset.y += e.yOffset;
		if (entity.isNaN()) {
			System.out.println("HOW DIS NAN " + entity.toString());
			entity.clear();
		}

		Vector3 r = ((new Vector3(e)).subtract(push))
				.add(entity);

		boolean rot = false;
		Vector3 rotation = boxRotation();

		if (!(rotation.y == 0 && rotation.z == 0)) {
			rot = true;
			r = r.rotateAboutAngles(rotation.y, -rotation.z);
		}
		List<Vector3> points = new ArrayList();
		for(Vector3 v1: corners())
		{
			points.add(v1);
		}
		Vector3 mid = Vector3.findMidPoint(points);
		
		r = r.subtract(offset);
		
		Vector3 min = boxMin();
		min.y = -e.yOffset;
		Vector3 max = boxMax();

		boolean flag = true;//!(e instanceof IMultiBox);

		double posxOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMax().x;
		double negxOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMin().x;
		double posyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
				.boxMax().y;
		double negyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
				.boxMin().y;
		double poszOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMax().z;
		double negzOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMin().z;

		if (flag
				&& !(r.inMatBox(this)
						|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(
								this)
						|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(
								this)

						|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(
								this) || r.add(
						new Vector3(0, 0, negzOffset)).inMatBox(this))) {
			// System.out.println();
			return ret;
		} else if (!(r.inMatBox(this)
				|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(this)
				|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(this)

				|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(this)
				|| r.add(new Vector3(0, 0, negzOffset)).inMatBox(this)

				|| r.add(new Vector3(0, posyOffset, 0)).inMatBox(this) || r
				.add(new Vector3(0, negyOffset, 0)).inMatBox(this)

		))

		{
			min.y = 0;
			return ret;
		}

		Vector3 pushDir = new Vector3();
		Vector3 pushLoc = new Vector3();

		double x = (r.x - (boxXLength()) / 2) / (boxXLength());
		double y = (r.y - (boxYLength()) / 2) / (boxYLength());
		double z = (r.z - (boxZLength()) / 2) / (boxZLength());
		double yoffset = 0;

		double yDiff = push.y + max.y + offset.y;
		double rho = Math.sqrt(r.x * r.x + r.z * r.z);

		if (rot) {
			yDiff += rho * Math.sin(-rotation.y);
		}

		boolean movedx = false, movedy = false, movedz = false;

		Vector3 location = new Vector3(x, y, z);

		if (location.isNaN()) {
			location.clear();
		}
		{
			boolean entitymovingup = (e.motionY - v.y) > 0;

			boolean zpositive = location.z > 0.5 && Math.abs(location.x)<0.5;
			boolean znegative = location.z < -0.5 && Math.abs(location.x)<0.5;

			boolean xpositive = location.x > 0.5 && Math.abs(location.z)<0.5;
			boolean xnegative = location.x < -0.5 && Math.abs(location.z)<0.5;
//
			boolean ynegative = (entitymovingup || (e.motionY - v.y) > 0.2)
					&& location.y < 0;
			boolean ypositive = Math.abs(yDiff - e.posY) <= e.stepHeight
					|| ((e.motionY - v.y) < -0.2 && (e.posY - e.motionY > yDiff));// &&!entitymovingup;//||(((pusher.motionY)<0)&&(yDiff-e.posY)<=e.stepHeight)||(flag&&(yDiff-e.posY)<=e.stepHeight);

			double f = 0.005;
			double  fx = f*10, 
					fy = f, 
					fz = f*10;//TODO make this speed based on the relative penetration and speed
			
			
			if (ypositive) {
				pushDir.y = v.y > 0 ? v.y : 0;
				e.motionY = v.y > 0 ? v.y : 0;
				e.setPosition(e.posX + v.x, yDiff, e.posZ + v.z);
				e.isAirBorne = false;
				e.onGround = true;
				e.fallDistance = 0;
				movedy = true;
			} else if (ynegative) {
				pushDir.y = -e.motionY - fy + v.y;
				movedy = true;
			}

			if (!movedy) {
				
				if (xnegative) {
					pushDir.x = -fx;
					pushLoc.x = min.x;
//					 System.out.println("pushed1"+location+" "+pushDir);//+" "+pushLoc+" "+pushDir.rotateAboutAngles(-rotation.y, -rotation.z));
					movedx = true;
				} else if (xpositive) {
					pushDir.x = fx;
					pushLoc.x = max.x;
//					 System.out.println("pushed2"+location+" "+pushDir);//+" "+pushDir.rotateAboutAngles(-rotation.y, -rotation.z));
					movedx = true;
				}
				if (znegative) {
					pushDir.z = -fz;
					pushLoc.z = min.z;
//					 System.out.println("pushed3"+location+" "+pushDir+" "+mid);//+" "+pushDir.rotateAboutAngles(-rotation.y, -rotation.z));
					movedz = true;
				} else if (zpositive) {
					pushDir.z = fz;
					pushLoc.z = max.z;
//					 System.out.println("pushed4"+location+" "+pushDir);//+" "+pushDir.rotateAboutAngles(-rotation.y, -rotation.z));
					movedz = true;
				}
			}

		}

		if (rot) {
			pushDir = pushDir.rotateAboutAngles(-rotation.y, rotation.z);
		}
		if (movedx || movedz) {
			
			if(!pushDir.isNaN())
			{
				e.motionX = pushDir.x;
				e.motionY = pushDir.y;
				e.motionZ = pushDir.z;
			}
			
			ret = movedx || movedz || movedy;
		}
		if (e instanceof EntityItem) {
			if (movedy) {
				e.motionX = 0;
				e.motionZ = 0;
			}
		}
		min.y = 0;
		return movedx || movedz || movedy;
	}

	public Vector3 getEntityCollisionVector(Entity pusher, Entity e,
			Vector3 offset, Vector3 entity) {
		return getCollisionVector(new Vector3(pusher), new Vector3(
				pusher.motionX, pusher.motionY, pusher.motionZ), e, offset,
				entity);
	}

	public Vector3 getCollisionVector(Vector3 push, Vector3 v, Entity e,
			Vector3 offset, Vector3 entity) {
		Vector3 ret = new Vector3();

		offset.y += e.yOffset;
		if (entity.isNaN()) {
			System.out.println("HOW DIS NAN " + entity.toString());
			entity.clear();
		}

		Vector3 r = ((new Vector3(e)).subtract(push))
				.add(entity);

		boolean rot = false;
		Vector3 rotation = boxRotation();

		if (!(rotation.y == 0 && rotation.z == 0)) {
			rot = true;
			r = r.rotateAboutAngles(rotation.y, -rotation.z);
		}
		
		r = r.subtract(offset);
		
		Vector3 min = boxMin();
		min.y = -e.yOffset;
		Vector3 max = boxMax();

		boolean flag = true;//!(e instanceof IMultiBox);

		double posxOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMax().x;
		double negxOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMin().x;
		double posyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
				.boxMax().y;
		double negyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
				.boxMin().y;
		double poszOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMax().z;
		double negzOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
				push).boxMin().z;

		if (flag
				&& !(r.inMatBox(this)
						|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(
								this)
						|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(
								this)

						|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(
								this) || r.add(
						new Vector3(0, 0, negzOffset)).inMatBox(this))) {
			// System.out.println();
			return ret;
		} else if (!(r.inMatBox(this)
				|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(this)
				|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(this)

				|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(this)
				|| r.add(new Vector3(0, 0, negzOffset)).inMatBox(this)

				|| r.add(new Vector3(0, posyOffset, 0)).inMatBox(this) || r
				.add(new Vector3(0, negyOffset, 0)).inMatBox(this)

		))

		{
			min.y = 0;
			return ret;
		}

		Vector3 pushDir = new Vector3();

		double x = (r.x - (boxXLength()) / 2) / (boxXLength());
		double y = (r.y - (boxYLength()) / 2) / (boxYLength());
		double z = (r.z - (boxZLength()) / 2) / (boxZLength());
		double yoffset = 0;

		double yDiff = push.y + max.y + offset.y;
		double rho = Math.sqrt(r.x * r.x + r.z * r.z);

		if (rot) {
			yDiff += rho * Math.sin(-rotation.y);
		}

		Vector3 location = new Vector3(x, y, z);

		if (location.isNaN()) {
			location.clear();
		}
		boolean entitymovingup = (e.motionY - v.y) > 0;

		boolean zpositive = location.z > 0.5 && Math.abs(location.x)<0.5;
		
		boolean znegative = location.z < -0.5 && Math.abs(location.x)<0.5;

		boolean xpositive = location.x > 0.5 && Math.abs(location.z)<0.5;
		boolean xnegative = location.x < -0.5 && Math.abs(location.z)<0.5;
//
		boolean ynegative = (entitymovingup || (e.motionY - v.y) > 0.2)
				&& location.y < 0;
		boolean ypositive = Math.abs(yDiff - e.posY) <= e.stepHeight
				|| ((e.motionY - v.y) < -0.2 && (e.posY - e.motionY > yDiff));// &&!entitymovingup;//||(((pusher.motionY)<0)&&(yDiff-e.posY)<=e.stepHeight)||(flag&&(yDiff-e.posY)<=e.stepHeight);

		double f = 0.005;
		double fx = f*50 * (Math.abs(location.x)), fy = f, fz = f*50 * (Math.abs(location.z));//TODO make this speed based on the relative penetration and speed
		boolean movedY = false;
		
		if (ypositive) {
			pushDir.y = v.y > 0 ? v.y : 0;
			movedY= true;
		} else if (ynegative) {
			movedY=true;
			pushDir.y = -e.motionY - fy + v.y;
		}

		if (!movedY) {
			if (xnegative) {
				pushDir.x = -fx;
			} else if (xpositive) {
				pushDir.x = fx;
			}
			if (znegative) {
				pushDir.z = -fz;
			} else if (zpositive) {
				pushDir.z = fz;
			}
		}
		if (rot) {
			pushDir = pushDir.rotateAboutAngles(-rotation.y, rotation.z);
		}
		min.y = 0;
		return pushDir;
	}

	public static boolean multiBoxIntersect(Entity pusher, Entity e,
			Vector3 offset, Vector3 intersect) {
		boolean ret = false;
		if (!(pusher instanceof IMultiBox && e instanceof IMultiBox))
			return ret;

		Vector3 entity = new Vector3(e, true);
		Vector3 push = new Vector3(pusher, true);

		for (String pusherKey : ((IMultiBox) pusher).getBoxes().keySet()) {
			for (String eKey : ((IMultiBox) e).getBoxes().keySet()) {
				Vector3 pushOffset = ((IMultiBox) pusher).getOffsets()
						.contains(pusherKey) ? ((IMultiBox) pusher)
						.getOffsets().get(pusherKey) : new Vector3();
				Vector3 eOffset = ((IMultiBox) e).getOffsets().contains(
						eKey) ? ((IMultiBox) e).getOffsets().get(eKey)
						: new Vector3();

				Matrix3 pushBox = ((IMultiBox) pusher).getBoxes()
						.get(pusherKey).addOffset(pushOffset)
						.addOffset(push);
				Matrix3 eBox = ((IMultiBox) e).getBoxes().get(eKey)
						.addOffset(eOffset).addOffset(entity);

				ret = pushBox.intersects(eBox);

				if (ret) {
					intersect = pushBox.intersect(eBox).subtract(entity);
					break;
				}
			}
			if (ret)
				break;
		}

		return ret;
	}

	public boolean intersects(Matrix3 otherBox) {
		boolean ret = false;
		Vector3[] corners = otherBox.corners();
		Vector3.rotateAboutAngles(corners, boxRotation());
		for (Vector3 r : corners) {
			if (r.inMatBox(this)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public Vector3 intersect(Matrix3 otherBox) {
		Vector3 ret = new Vector3();
		Vector3[] corners = otherBox.corners();
		Vector3.rotateAboutAngles(corners, boxRotation());
		for (Vector3 r : corners) {
			if (r.inMatBox(this)) {
				ret = r;
				break;
			}
		}
		return ret;
	}

	public Vector3 getMatrixPushDir(Matrix3 box) {
		Vector3 ret = new Vector3();

		return ret;
	}

	public Vector3 getPushDirOutOfBlocks(Entity e, Vector3 offset) {
		Vector3 dir = new Vector3();

		return dir;
	}

	public void pushEntityOutOfAABB(Matrix3 aabb, Entity e, Vector3 source) {
		double xMin = aabb.boxMin().x;
		double xMax = aabb.boxMax().x;

		double yMin = aabb.boxMin().y;
		double yMax = aabb.boxMax().y;

		double zMin = aabb.boxMin().z;
		double zMax = aabb.boxMax().z;

		Vector3 r = new Vector3(e);

		double posxOffset = e.width / 2;
		double negxOffset = -e.width / 2;
		double posyOffset = 0;
		double negyOffset = 0;
		double poszOffset = e.width / 2;
		double negzOffset = -e.width / 2;

		if (!(r.inMatBox(this)
				|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(this)
				|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(this)

				|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(this)
				|| r.add(new Vector3(0, 0, negzOffset)).inMatBox(this)

				|| r.add(new Vector3(0, posyOffset, 0)).inMatBox(this) || r
				.add(new Vector3(0, negyOffset, 0)).inMatBox(this)

		))

		{
			return;
		}

		Vector3 mid = new Vector3();
		mid.x = (xMin - xMax) / 2;
		mid.y = (yMin - yMax) / 2;
		mid.z = (zMin - zMax) / 2;
		source = source.add(mid);
		Vector3 finalPos = new Vector3();

		double xDiff = Math.abs((r.x - source.x) / aabb.boxXLength());
		double yDiff = Math.abs((r.y - source.y) / aabb.boxYLength());
		double zDiff = Math.abs((r.z - source.z) / aabb.boxZLength());

		if (r.x < source.x && xDiff > zDiff) {
			finalPos.x = source.x - xMin - 0.05;
		}
		if (r.y < source.y) {
			finalPos.y = source.y - yMin - 0.05;
		}

		if (r.z < source.z && zDiff > xDiff) {
			finalPos.z = source.z - zMin - 0.05;
		}

		if (r.x > source.x && xDiff > zDiff) {
			finalPos.x = source.x + xMax + 0.05;
		}
		if (r.y > source.y) {
			finalPos.y = source.y + yMax + 0.05;
		}

		if (r.z > source.z && zDiff > xDiff) {
			finalPos.z = source.z + zMax + 0.05;
		}
		finalPos.moveEntity(e);

	}

}
