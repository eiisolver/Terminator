package commander;

import com.aisandbox.util.Vector2;

/**
 * Used to find the "best" angle. The circle is divided into segments
 * like a bicycle wheel, each segment is assigned a value.
 * @author louis
 *
 */
public class Wheel {
   public static final int NR_SEGMENTS = 72;
   public int nrVisibleSegments = 6;
   public int[] values = new int[NR_SEGMENTS];
   private double fovAngle;
   private int bestValue;
   
   public void setFovAngle(double fovAngle) {
      this.fovAngle = fovAngle;
      nrVisibleSegments = NR_SEGMENTS / (int) Math.round(2*Math.PI/fovAngle);
      //System.out.println("nrVisible segments: " + nrVisibleSegments);
   }
   
   /**
    * Increases the value of the wheel segment with the given angle.
    * @param angle
    * @param value
    */
   public void addValue(double angle, int value) {
      int segmentNr = (int)(NR_SEGMENTS * (angle + Math.PI)/(2*Math.PI));
      segmentNr = Math.max(0, Math.min(values.length-1, segmentNr));
      values[segmentNr] += value;
      //System.out.println("add value " + angle + ", " + value + " -> segment " + segmentNr + " = " + values[segmentNr]);
   }
   
   public Vector2 getBestFacingDirection() {
      int seg = getBestSegmentNr();
      double segAngle = seg*2*Math.PI/NR_SEGMENTS - Math.PI;
      double facingAngle = segAngle + fovAngle/2;
      return Utils.facingAngleToVector2(facingAngle);
   }
   
   public int getBestSegmentNr() {
      bestValue = -100000;
      int bestSeg = 0;
      for (int i = 0; i < values.length; ++i) {
         int value = getValue(i);
         if (value > bestValue) {
            bestValue = value;
            bestSeg = i;
         }
      }
      return bestSeg;
   }
   
   /**
    * Get value of the segment/facing dir returned by getBestFacingDirection/getBestSegmentNr
    * (must first call best facing dir before calling getBestValue)
    * @return
    */
   public int getBestValue() {
      return bestValue;
   }
   
   /**
    * Decreases value of segments starting with segmentNr
    * @param segmentNr
    * @param val
    */
   public void markUsed(int segmentNr, int value) {
      for (int i = 0; i < nrVisibleSegments; ++i) {
         int seg = (segmentNr + i)%NR_SEGMENTS;
         values[seg] = Math.min(values[seg], value);
      }
   }
   
   private int getValue(int segmentNr) {
      int result = 0;
      for (int i = 0; i < nrVisibleSegments; ++i) {
         int seg = (segmentNr + i)%NR_SEGMENTS;
         result += values[seg];
      }
      return result;
   }

}
