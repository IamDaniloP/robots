package jk.mega;
 
import java.util.*;
 
public class BufferManager {
 
 //Featuring "Slice retrieval indexes"
   static final float[] empty = {};
    
   static final float[] velSlicesRough = {2.01f ,4.01f ,6.01f};
   static final float[] velSlices = {1.01f ,3.01f ,5.01f ,7.01f};
   static final float[] velSlicesFine = {0.5f, 1.5f ,2.5f ,3.5f ,4.5f ,5.5f ,6.5f ,7.5f};
      
   static final float[] advVelSlicesRough = {-4.01f,-2.01f,1.51f};
   static final float[] advVelSlices = {-5f, -3f, -1f, 2f};
   static final float[] advVelSlicesFine = {-6.5f, -5f, -3.5f, -2f, -0.5f, 0.5f, 2f, 4f}; 
   
   static final float[] bftSlicesRough = {20f ,40f ,60f};
   static final float[] bftSlices = {15f , 30f , 45f , 65f};
   static final float[] bftSlicesFine = {6f , 10f , 25f , 34f , 42f , 50f , 58f , 66f};
      
   static final float[] tsdcSlicesRough = {0.25f ,0.5f ,0.75f, 1f, 1.25f,1.5f,1.75f};
   static final float[] tsdcSlices = {0.2f ,0.4f ,0.6f ,0.8f, 1f, 1.2f, 1.4f, 1.6f,1.8f};
   static final float[] tsdcSlicesFine = {0.1f ,0.2f ,0.3f ,0.4f ,0.5f ,0.6f ,0.8f, 0.9f, 1f, 1.1f, 1.2f, 1.3f,1.4f,1.5f};
      
   static final float[] accelSlices = {-0.4f ,0.4f};
   static final float[] accelSlicesFine = {-2.1f, -1.9f, 0.9f, 1.1f};
      
   static final float[] tsvcSlicesRough = {0.15f ,0.357f ,0.75f, 0.9f, 1.2f};
   static final float[] tsvcSlices = {0.1f ,0.2f ,0.4f ,0.8f, 0.92f, 1.0f};
   static final float[] tsvcSlicesFine = {0.05f ,0.1f ,0.16f ,0.2f ,0.4f ,0.6f ,0.85f, 0.96f, 1.1f, 1.3f};
      
   static final float[] dl10SlicesRough = {20f ,50f ,80f};
   static final float[] dl10Slices = {10f ,20f ,35f ,50f ,68f ,85f};
   static final float[] dl10SlicesFine = {5f, 15f , 25f, 35f, 45f, 55f ,65f ,75f ,85f, 95f};
      
   static final float[] wallSlicesRough = {0.33f , 0.66f, 1f};
   static final float[] wallSlices = {0.25f , 0.5f , 0.75f, 1.1f};

   public static ArrayList getStatBuffers(){
      ArrayList statBuffers = new ArrayList(120);
      final float[][][] sets = new float[][][] {  
            //empty
            {empty, empty, empty, empty, empty, empty, empty, empty, empty},
            
            //simple
             {velSlices, advVelSlicesRough, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {empty, advVelSlices, bftSlicesFine, empty, empty, empty, empty, empty, empty},
            {empty, advVelSlicesFine, bftSlicesFine, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, empty, empty, empty, empty, empty, empty, empty, empty},
            {empty, advVelSlicesFine, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, empty, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {velSlices, advVelSlices, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesFine, empty, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, advVelSlices, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlicesFine, empty, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, empty, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, empty, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {empty, advVelSlicesRough, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, advVelSlices, bftSlicesFine, empty, empty, empty, empty, empty, empty},
            {velSlices, advVelSlicesFine, empty, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, empty, empty, empty, empty, empty, empty, empty},
            {velSlices, advVelSlicesRough, empty, empty, empty, empty, empty, empty, empty},
            {velSlices, advVelSlicesRough, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, empty, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesFine, empty, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlicesFine, advVelSlicesRough, empty, empty, empty, empty, empty, empty, empty},
            {velSlices, advVelSlices, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {empty, advVelSlicesRough, bftSlicesFine, empty, empty, empty, empty, empty, empty},
            
            //Anti-PM
            {velSlicesRough, advVelSlicesFine, empty, tsdcSlicesFine, empty, empty, dl10Slices, empty, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, empty, tsvcSlices, dl10SlicesRough, empty, empty},
            {velSlices, advVelSlicesFine, empty, tsdcSlices, empty, tsvcSlicesFine, empty, empty, empty},
            {velSlicesFine, empty, empty, tsdcSlicesFine, accelSlices, tsvcSlicesRough, empty, empty, empty},
            {velSlices, advVelSlicesRough, empty, empty, accelSlices, empty, dl10SlicesFine, empty, empty},
            {velSlicesRough, advVelSlices, empty, tsdcSlicesRough, empty, empty, dl10SlicesRough, empty, empty},
            {velSlicesFine, advVelSlicesRough, empty, empty, empty, tsvcSlices, dl10Slices, empty, empty},
            {empty, advVelSlicesRough, empty, empty, accelSlices, tsvcSlicesRough, dl10Slices, empty, empty},
            {empty, empty, empty, tsdcSlicesFine, accelSlices, tsvcSlicesRough, dl10SlicesFine, empty, empty},
            {velSlicesRough, empty, empty, tsdcSlices, accelSlices, empty, dl10SlicesFine, empty, empty},
            {velSlicesFine, advVelSlices, empty, tsdcSlices, empty, tsvcSlices, empty, empty, empty},
            {velSlicesFine, advVelSlices, empty, tsdcSlices, accelSlices, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, empty, empty, accelSlices, empty, dl10SlicesRough, empty, empty},
            {velSlicesRough, empty, empty, tsdcSlicesFine, empty, tsvcSlicesRough, dl10SlicesFine, empty, empty},
            {velSlices, empty, empty, tsdcSlicesRough, empty, tsvcSlices, dl10Slices, empty, empty},
            {empty, advVelSlices, empty, tsdcSlicesFine, accelSlices, tsvcSlicesFine, empty, empty, empty},
            {velSlices, advVelSlicesFine, empty, tsdcSlicesFine, accelSlices, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, empty, empty, accelSlices, tsvcSlicesRough, empty, empty, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, accelSlices, tsvcSlicesRough, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, empty, empty, accelSlices, tsvcSlicesRough, empty, empty, empty},
            {velSlicesRough, empty, empty, tsdcSlices, accelSlices, tsvcSlicesRough, empty, empty, empty},
            {velSlicesFine, advVelSlicesFine, empty, tsdcSlices, accelSlices, empty, empty, empty, empty},
            {velSlicesFine, advVelSlicesFine, empty, empty, empty, tsvcSlicesFine, dl10SlicesFine, empty, empty},
            {velSlicesRough, advVelSlices, empty, tsdcSlicesRough, accelSlices, empty, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, empty, tsdcSlices, empty, tsvcSlicesFine, empty, empty, empty},
            {empty, advVelSlices, empty, tsdcSlices, empty, tsvcSlices, dl10SlicesFine, empty, empty},
            {velSlices, advVelSlices, empty, empty, accelSlices, tsvcSlicesRough, empty, empty, empty},
            {velSlicesFine, advVelSlicesFine, empty, empty, accelSlices, empty, dl10SlicesRough, empty, empty},
            {empty, advVelSlicesRough, empty, tsdcSlicesFine, accelSlices, empty, dl10SlicesRough, empty, empty},
            {velSlicesFine, advVelSlicesRough, empty, tsdcSlicesRough, accelSlices, empty, empty, empty, empty}                  ,
            //main
            {velSlicesRough, empty, empty, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, empty, empty, empty, empty, empty, empty, empty},
            {velSlicesFine, advVelSlicesFine, empty, empty, empty, tsvcSlices, dl10SlicesRough, empty, empty},
            {velSlices, advVelSlices, bftSlicesFine, empty, accelSlices, empty, empty, wallSlices, empty},
            {velSlicesRough, empty, empty, tsdcSlices, empty, tsvcSlicesFine, dl10Slices, empty, wallSlicesRough},
            {velSlices, advVelSlicesFine, empty, empty, empty, tsvcSlicesFine, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, empty, empty, empty, tsvcSlicesRough, empty, wallSlicesRough, empty},
            {empty, advVelSlicesFine, bftSlicesRough, tsdcSlicesFine, empty, empty, dl10Slices, wallSlicesRough, empty},
            {velSlices, empty, empty, empty, accelSlices, tsvcSlices, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, bftSlices, empty, empty, empty, empty, wallSlicesRough, wallSlicesRough},
            {velSlicesFine, advVelSlicesFine, bftSlicesRough, empty, empty, empty, empty, empty, empty},
            {empty, empty, bftSlices, empty, accelSlices, empty, dl10SlicesFine, wallSlices, empty},
            {empty, advVelSlicesRough, bftSlices, empty, accelSlices, empty, empty, empty, empty},
            {empty, advVelSlices, bftSlicesRough, empty, empty, empty, empty, wallSlicesRough, empty},
            {velSlicesRough, empty, bftSlicesRough, empty, accelSlices, empty, empty, empty, empty},
            {velSlices, advVelSlicesRough, empty, tsdcSlicesRough, empty, empty, empty, wallSlicesRough, wallSlicesRough},
            {velSlicesRough, empty, empty, tsdcSlicesRough, accelSlices, empty, empty, wallSlices, empty},
            {empty, advVelSlicesFine, bftSlices, empty, empty, empty, dl10SlicesFine, wallSlicesRough, empty},
            {velSlices, advVelSlicesFine, bftSlicesRough, tsdcSlicesFine, empty, empty, empty, wallSlicesRough, empty},
            {velSlices, advVelSlicesFine, bftSlicesRough, tsdcSlicesRough, empty, tsvcSlices, empty, empty, empty},
            {velSlicesFine, empty, bftSlices, empty, empty, tsvcSlices, empty, wallSlicesRough, empty},
            {velSlices, advVelSlices, empty, tsdcSlicesFine, accelSlices, empty, empty, empty, wallSlicesRough},
            {velSlicesFine, advVelSlicesFine, empty, empty, accelSlices, tsvcSlicesRough, empty, empty, wallSlicesRough},
            {velSlices, empty, bftSlicesRough, empty, accelSlices, empty, dl10Slices, wallSlicesRough, empty},
            {velSlicesRough, empty, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlicesFine, advVelSlicesFine, bftSlices, empty, empty, empty, empty, wallSlices, wallSlicesRough},
            {velSlices, empty, bftSlicesRough, tsdcSlicesRough, accelSlices, empty, empty, wallSlicesRough, empty},
            {velSlicesRough, advVelSlicesFine, bftSlices, empty, empty, tsvcSlices, empty, empty, wallSlicesRough},
            {velSlicesRough, advVelSlicesRough, bftSlicesRough, tsdcSlices, empty, empty, empty, empty, wallSlicesRough},
            {velSlicesFine, advVelSlicesRough, empty, empty, empty, tsvcSlicesFine, empty, wallSlices, wallSlicesRough},
            {velSlicesRough, empty, bftSlices, empty, empty, empty, empty, wallSlicesRough, empty},
            {velSlices, empty, bftSlicesFine, tsdcSlicesRough, empty, empty, dl10SlicesFine, wallSlices, empty},
            {empty, advVelSlicesFine, empty, empty, accelSlices, tsvcSlicesFine, empty, empty, empty},
            {empty, empty, bftSlicesRough, tsdcSlices, accelSlices, tsvcSlicesFine, dl10SlicesRough, empty, empty},
            {velSlicesRough, advVelSlicesRough, empty, empty, accelSlices, empty, dl10SlicesRough, empty, wallSlicesRough},
            {empty, advVelSlicesFine, bftSlices, tsdcSlices, empty, tsvcSlicesRough, empty, wallSlicesRough, wallSlicesRough},
            {velSlicesRough, empty, bftSlices, tsdcSlicesFine, empty, empty, empty, empty, empty},
            {velSlicesRough, empty, bftSlicesFine, tsdcSlices, accelSlices, empty, dl10SlicesFine, empty, empty},
            {velSlices, empty, empty, empty, accelSlices, empty, empty, empty, wallSlicesRough},
            {velSlicesFine, empty, bftSlices, empty, empty, tsvcSlicesFine, empty, empty, empty},
            {velSlicesFine, advVelSlices, bftSlicesFine, tsdcSlicesRough, empty, empty, empty, empty, wallSlicesRough},
            {velSlices, advVelSlices, bftSlicesRough, empty, empty, empty, dl10Slices, wallSlices, empty},
            {velSlices, advVelSlicesFine, empty, empty, accelSlices, tsvcSlicesFine, dl10SlicesFine, empty, empty},
            {velSlicesRough, advVelSlicesRough, empty, empty, accelSlices, empty, empty, wallSlices, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, accelSlices, empty, empty, wallSlices, empty},
            {velSlicesFine, empty, empty, tsdcSlices, empty, tsvcSlicesRough, empty, wallSlicesRough, empty},
            {velSlicesRough, empty, empty, tsdcSlices, empty, empty, dl10Slices, wallSlicesRough, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, accelSlices, empty, empty, empty, empty},
            {velSlices, advVelSlices, bftSlicesFine, empty, empty, tsvcSlicesFine, empty, empty, empty},
            {velSlices, advVelSlicesRough, empty, empty, empty, tsvcSlicesRough, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, empty, empty, accelSlices, tsvcSlices, dl10SlicesFine, wallSlices, empty},
            {velSlicesRough, advVelSlicesRough, bftSlicesFine, empty, empty, empty, empty, wallSlices, wallSlicesRough},
            {velSlices, advVelSlicesRough, bftSlicesFine, empty, empty, empty, empty, empty, empty},
            {velSlices, empty, bftSlices, empty, accelSlices, tsvcSlicesRough, dl10SlicesFine, empty, empty},
            {velSlicesRough, advVelSlices, bftSlicesRough, empty, empty, empty, empty, wallSlicesRough, empty},
            {empty, empty, bftSlices, empty, empty, empty, dl10SlicesRough, wallSlicesRough, empty},
            {velSlices, empty, bftSlices, tsdcSlicesRough, accelSlices, empty, dl10SlicesFine, empty, empty},
            {empty, advVelSlices, empty, empty, empty, tsvcSlices, dl10Slices, wallSlices, empty},
            {velSlicesFine, empty, empty, tsdcSlices, accelSlices, tsvcSlicesRough, empty, wallSlicesRough, wallSlicesRough},
            {velSlices, advVelSlices, bftSlices, empty, empty, empty, empty, empty, empty},
            {velSlices, advVelSlicesFine, bftSlicesFine, empty, accelSlices, empty, empty, empty, wallSlicesRough},
            {velSlicesRough, empty, bftSlices, empty, accelSlices, empty, empty, wallSlices, wallSlicesRough},
            {velSlicesFine, empty, empty, empty, empty, empty, empty, wallSlices, wallSlicesRough},
            {empty, advVelSlicesFine, bftSlicesRough, empty, empty, tsvcSlicesRough, dl10SlicesRough, empty, wallSlicesRough},
            {velSlices, advVelSlices, bftSlicesRough, empty, empty, tsvcSlicesFine, dl10SlicesFine, wallSlicesRough, empty},
            {velSlices, empty, empty, empty, empty, tsvcSlicesRough, empty, wallSlices, empty},
            {velSlices, advVelSlicesFine, bftSlices, empty, empty, empty, empty, empty, wallSlicesRough},
            {empty, empty, bftSlicesRough, empty, empty, empty, dl10SlicesRough, wallSlicesRough, wallSlicesRough},
            {velSlices, advVelSlicesRough, bftSlicesFine, empty, accelSlices, empty, empty, empty, wallSlicesRough},
            // {velSlicesRough, empty, bftSlicesFine, empty, accelSlices, empty, dl10SlicesFine, wallSlices, empty},
            // {velSlicesFine, empty, empty, tsdcSlicesRough, accelSlices, empty, dl10SlicesRough, wallSlices, empty},
            // {velSlicesFine, advVelSlicesRough, bftSlicesFine, empty, accelSlices, empty, dl10Slices, empty, empty},
            // {velSlices, advVelSlicesRough, empty, tsdcSlices, empty, tsvcSlicesFine, empty, wallSlicesRough, empty},
            // {velSlices, advVelSlices, bftSlicesFine, empty, accelSlices, empty, dl10Slices, empty, empty},
            // {velSlicesRough, empty, bftSlicesFine, tsdcSlicesRough, empty, empty, empty, wallSlices, empty},
            // {velSlices, advVelSlicesFine, bftSlicesFine, tsdcSlicesRough, accelSlices, empty, empty, empty, empty},
            // {empty, empty, bftSlicesRough, tsdcSlices, empty, empty, dl10Slices, wallSlices, wallSlicesRough},
            // {velSlices, empty, bftSlicesFine, empty, empty, empty, empty, wallSlicesRough, empty},
            // {velSlicesFine, advVelSlices, empty, tsdcSlices, empty, empty, empty, wallSlices, empty},
            // {velSlicesRough, advVelSlicesRough, empty, empty, empty, empty, empty, wallSlicesRough, empty},
            // {velSlicesRough, empty, empty, empty, accelSlices, empty, dl10SlicesRough, empty, empty},
            // {velSlicesFine, advVelSlicesFine, empty, empty, accelSlices, tsvcSlicesRough, empty, empty, empty},
            // {velSlices, advVelSlicesFine, empty, empty, empty, empty, dl10SlicesRough, wallSlicesRough, empty},
            // {velSlicesFine, advVelSlicesFine, bftSlicesRough, empty, empty, empty, empty, wallSlicesRough, empty},
            // {velSlicesFine, advVelSlicesRough, bftSlicesRough, empty, empty, empty, dl10Slices, empty, empty},
            // {empty, empty, bftSlicesRough, empty, empty, tsvcSlicesFine, dl10Slices, empty, empty},
            // {empty, advVelSlicesFine, bftSlices, empty, accelSlices, empty, empty, wallSlices, empty},
            // {velSlices, empty, bftSlicesRough, empty, empty, empty, empty, wallSlices, wallSlicesRough},
            // {velSlicesFine, empty, bftSlices, empty, accelSlices, empty, empty, wallSlicesRough, empty},
            // {velSlicesRough, empty, empty, tsdcSlicesFine, accelSlices, empty, empty, wallSlicesRough, empty},
            // {velSlicesFine, empty, empty, empty, accelSlices, empty, empty, wallSlices, empty},
            // {velSlicesRough, empty, bftSlicesRough, empty, empty, tsvcSlicesFine, empty, empty, empty},
            // {empty, empty, bftSlices, empty, accelSlices, empty, dl10SlicesRough, wallSlices, empty},
            // {velSlices, advVelSlicesFine, empty, empty, empty, tsvcSlices, empty, wallSlices, empty},
            // {velSlices, advVelSlicesRough, bftSlicesFine, empty, empty, empty, empty, wallSlices, empty},
            // {velSlices, empty, bftSlices, tsdcSlices, empty, empty, empty, empty, empty},
            // {velSlices, empty, bftSlicesFine, empty, empty, empty, empty, empty, empty},
            // {velSlices, advVelSlicesFine, empty, empty, accelSlices, tsvcSlices, dl10SlicesRough, wallSlices, empty},
            // {velSlicesRough, advVelSlices, bftSlicesFine, tsdcSlices, empty, empty, empty, wallSlicesRough, empty},
            // {velSlices, empty, bftSlicesFine, tsdcSlicesFine, accelSlices, empty, empty, wallSlicesRough, empty},
            // {velSlices, advVelSlices, bftSlices, empty, empty, empty, dl10Slices, wallSlicesRough, empty},
            // {velSlicesFine, empty, empty, empty, accelSlices, empty, empty, wallSlicesRough, empty}
            }      ;
                             
      putBuffersInto(sets,statBuffers);
      
      return statBuffers;
   }
   public static ArrayList getFlattenerBuffers(){
      ArrayList flattenerBuffers = new ArrayList(50);  
                 
      float[][][] flatSets = new float[][][] { 
            {empty, empty, bftSlicesRough, empty, accelSlices, tsvcSlicesRough, empty, wallSlices, wallSlicesRough},
            {empty, advVelSlices, bftSlicesRough, empty, empty, empty, dl10SlicesFine, wallSlicesRough, wallSlicesRough},
            {empty, advVelSlices, empty, empty, accelSlices, tsvcSlicesFine, empty, empty, empty},
            {velSlicesRough, advVelSlicesRough, bftSlicesRough, empty, empty, empty, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, bftSlicesRough, empty, accelSlices, empty, empty, empty, wallSlicesRough},
            {empty, empty, empty, tsdcSlices, empty, tsvcSlicesFine, empty, wallSlices, empty},
            {empty, advVelSlicesFine, bftSlices, tsdcSlicesFine, empty, tsvcSlicesRough, dl10SlicesRough, empty, empty},
            {empty, advVelSlices, empty, empty, accelSlices, empty, empty, wallSlicesRough, empty},
            {velSlicesRough, empty, bftSlicesRough, empty, accelSlices, tsvcSlicesRough, empty, wallSlices, empty},
            {empty, advVelSlices, empty, empty, accelSlices, tsvcSlicesRough, dl10Slices, empty, empty},
            {empty, advVelSlicesRough, bftSlicesFine, empty, accelSlices, empty, dl10SlicesRough, empty, empty},
            {empty, empty, bftSlices, empty, empty, tsvcSlices, dl10Slices, empty, wallSlicesRough},
            {velSlicesFine, empty, bftSlices, empty, accelSlices, tsvcSlicesFine, empty, empty, empty},
            {empty, advVelSlicesRough, bftSlicesRough, empty, empty, empty, empty, wallSlices, wallSlicesRough},
            {empty, advVelSlices, bftSlicesRough, tsdcSlices, empty, tsvcSlicesFine, dl10SlicesRough, empty, wallSlicesRough},
            {velSlicesFine, empty, bftSlices, empty, accelSlices, tsvcSlicesFine, dl10Slices, empty, wallSlicesRough},
            {empty, empty, bftSlices, tsdcSlicesRough, empty, tsvcSlicesRough, dl10SlicesRough, empty, wallSlicesRough},
            {velSlicesRough, empty, bftSlicesFine, empty, accelSlices, empty, empty, wallSlicesRough, empty},
            {empty, empty, bftSlices, empty, accelSlices, empty, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, empty, empty, empty, tsvcSlices, dl10SlicesFine, wallSlicesRough, empty},
            {velSlicesRough, empty, empty, tsdcSlicesFine, empty, tsvcSlices, empty, wallSlicesRough, wallSlicesRough},
            {empty, empty, bftSlicesRough, empty, accelSlices, tsvcSlices, empty, empty, wallSlicesRough},
            {velSlicesRough, advVelSlices, bftSlices, empty, accelSlices, empty, empty, empty, wallSlicesRough},
            {velSlicesRough, advVelSlices, bftSlices, tsdcSlices, accelSlices, empty, empty, wallSlices, empty},
            {velSlicesFine, advVelSlicesRough, bftSlicesRough, empty, empty, empty, empty, wallSlices, empty},
            {empty, empty, empty, tsdcSlices, accelSlices, tsvcSlicesRough, empty, wallSlicesRough, wallSlicesRough},
            {velSlicesRough, empty, empty, tsdcSlicesRough, accelSlices, empty, empty, empty, wallSlicesRough},
            {empty, advVelSlicesFine, empty, tsdcSlicesFine, empty, empty, dl10SlicesFine, empty, empty},
            {empty, advVelSlices, empty, empty, empty, tsvcSlicesRough, dl10Slices, wallSlices, wallSlicesRough},
            {empty, empty, bftSlicesFine, empty, empty, empty, dl10SlicesFine, wallSlices, wallSlicesRough},
            {velSlices, advVelSlicesFine, empty, tsdcSlices, empty, tsvcSlices, empty, empty, wallSlicesRough},
            {empty, advVelSlicesFine, empty, tsdcSlices, empty, empty, dl10Slices, wallSlices, empty},
            {empty, advVelSlicesFine, empty, tsdcSlicesFine, accelSlices, empty, empty, wallSlices, wallSlicesRough},
            {velSlicesRough, empty, empty, empty, empty, empty, dl10Slices, wallSlicesRough, wallSlicesRough},
            {velSlicesFine, empty, empty, tsdcSlicesRough, accelSlices, tsvcSlicesRough, empty, wallSlicesRough, wallSlicesRough},
            {empty, advVelSlices, bftSlices, empty, accelSlices, tsvcSlicesRough, dl10SlicesRough, empty, wallSlicesRough},
            {empty, empty, bftSlices, tsdcSlicesFine, empty, empty, dl10Slices, wallSlicesRough, wallSlicesRough},
            {velSlicesFine, empty, bftSlicesRough, tsdcSlicesRough, accelSlices, empty, dl10SlicesFine, empty, empty},
            {velSlicesRough, empty, empty, empty, empty, tsvcSlicesFine, empty, wallSlices, empty},
            {velSlices, empty, empty, empty, empty, tsvcSlices, dl10SlicesFine, empty, wallSlicesRough},
            {empty, advVelSlices, bftSlicesFine, empty, empty, tsvcSlices, empty, wallSlicesRough, wallSlicesRough},
            {empty, empty, bftSlices, tsdcSlicesRough, accelSlices, empty, empty, empty, wallSlicesRough},
            {empty, advVelSlicesFine, bftSlicesRough, tsdcSlicesRough, accelSlices, empty, dl10SlicesFine, empty, empty},
            {empty, advVelSlicesFine, bftSlicesFine, tsdcSlices, empty, empty, dl10SlicesFine, wallSlicesRough, empty},
            {velSlicesFine, advVelSlicesFine, bftSlices, empty, accelSlices, empty, empty, empty, empty},
            {empty, advVelSlicesFine, bftSlicesFine, empty, empty, empty, dl10SlicesFine, wallSlicesRough, empty},
            {empty, advVelSlices, empty, empty, accelSlices, tsvcSlicesFine, empty, empty, wallSlicesRough},
            {empty, empty, empty, tsdcSlicesFine, accelSlices, tsvcSlices, dl10Slices, empty, empty},
            {velSlicesRough, empty, empty, empty, accelSlices, tsvcSlices, dl10SlicesFine, empty, empty},
            {empty, advVelSlicesFine, empty, tsdcSlices, accelSlices, empty, empty, empty, wallSlicesRough}
            };
   
      putBuffersInto(flatSets,flattenerBuffers);
      return flattenerBuffers;
   			
   }
   public static ArrayList getABSBuffers(){
      ArrayList ABSBuffers = new ArrayList(50);  
                 
      float[][][] flatSets = new float[][][] { 
            {velSlicesRough, empty, empty, empty, empty, tsvcSlicesFine, empty, wallSlicesRough, empty},
            {empty, empty, bftSlicesRough, empty, empty, empty, dl10SlicesFine, wallSlices, empty},
            {empty, empty, bftSlices, tsdcSlicesRough, empty, tsvcSlicesRough, empty, wallSlicesRough, wallSlicesRough},
            {velSlices, advVelSlices, bftSlicesRough, empty, empty, tsvcSlicesRough, empty, empty, wallSlicesRough},
            {empty, empty, bftSlicesRough, empty, empty, tsvcSlicesRough, empty, wallSlices, empty},
            {velSlicesFine, advVelSlicesFine, bftSlices, empty, empty, tsvcSlices, empty, empty, wallSlicesRough},
            {empty, empty, bftSlicesFine, tsdcSlicesRough, empty, tsvcSlicesFine, empty, wallSlices, empty},
            {velSlicesRough, empty, empty, empty, accelSlices, tsvcSlicesFine, empty, empty, wallSlicesRough},
            {empty, empty, bftSlicesRough, tsdcSlicesFine, accelSlices, tsvcSlicesFine, dl10SlicesFine, empty, empty},
            {velSlicesRough, empty, empty, tsdcSlices, accelSlices, tsvcSlices, empty, empty, empty},
            {velSlicesFine, empty, bftSlicesFine, empty, empty, empty, dl10SlicesRough, wallSlicesRough, wallSlicesRough},
            {velSlicesRough, advVelSlicesRough, empty, tsdcSlices, empty, tsvcSlices, empty, empty, wallSlicesRough},
            {empty, advVelSlicesRough, empty, empty, accelSlices, tsvcSlices, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, empty, empty, empty, tsvcSlicesFine, dl10SlicesFine, wallSlices, wallSlicesRough},
            {empty, advVelSlicesRough, empty, empty, accelSlices, tsvcSlicesRough, dl10Slices, empty, empty},
            {velSlicesRough, empty, bftSlicesFine, tsdcSlicesRough, accelSlices, empty, empty, wallSlicesRough, empty},
            {velSlicesFine, empty, bftSlices, tsdcSlicesRough, empty, empty, empty, empty, empty},
            {velSlicesFine, advVelSlices, bftSlicesFine, empty, empty, tsvcSlices, empty, empty, empty},
            {velSlicesRough, advVelSlicesFine, empty, empty, empty, tsvcSlicesFine, empty, wallSlices, empty},
            {empty, empty, bftSlices, tsdcSlices, empty, tsvcSlicesRough, dl10Slices, empty, empty},
            {empty, empty, bftSlices, empty, accelSlices, tsvcSlicesFine, empty, wallSlices, wallSlicesRough},
            {empty, advVelSlicesFine, empty, empty, accelSlices, tsvcSlices, empty, wallSlicesRough, empty},
            {velSlicesRough, advVelSlicesRough, bftSlices, empty, empty, tsvcSlices, dl10SlicesFine, empty, empty},
            {empty, empty, empty, tsdcSlices, empty, tsvcSlicesRough, dl10SlicesRough, empty, empty},
            {velSlicesFine, empty, empty, tsdcSlices, empty, empty, empty, wallSlices, wallSlicesRough},
            {empty, empty, bftSlicesFine, empty, empty, tsvcSlices, dl10SlicesFine, empty, wallSlicesRough},
            {velSlicesFine, empty, empty, tsdcSlicesRough, empty, empty, dl10Slices, wallSlices, empty},
            {empty, empty, empty, empty, accelSlices, tsvcSlices, dl10SlicesFine, empty, wallSlicesRough},
            {empty, empty, empty, tsdcSlicesFine, accelSlices, tsvcSlices, dl10SlicesRough, empty, empty},
            {empty, empty, bftSlicesRough, tsdcSlicesFine, accelSlices, tsvcSlices, empty, wallSlicesRough, wallSlicesRough}
            }      ;
      putBuffersInto(flatSets,ABSBuffers);
      return ABSBuffers;
   			
   }
   public static ArrayList getFlattenerTickBuffers(){
      ArrayList flattenerTickBuffers = new ArrayList(15);  
                 
      float[][][] flatTickSets = new float[][][] {
            {empty, empty, empty, empty, accelSlices, empty, dl10SlicesFine, wallSlices, wallSlicesRough},
            {velSlicesFine, empty, empty, tsdcSlices, empty, empty, dl10SlicesRough, empty, wallSlicesRough},
            {velSlicesFine, empty, bftSlices, empty, accelSlices, empty, empty, wallSlicesRough, wallSlicesRough},
            {empty, advVelSlices, empty, empty, accelSlices, empty, dl10SlicesFine, empty, empty},
            {empty, advVelSlicesFine, bftSlicesFine, tsdcSlicesFine, accelSlices, empty, dl10SlicesFine, empty, empty},
            {velSlices, empty, empty, tsdcSlices, empty, tsvcSlicesRough, empty, wallSlices, wallSlicesRough},
            {empty, advVelSlicesFine, bftSlicesRough, tsdcSlicesFine, empty, empty, dl10SlicesRough, empty, empty},
            {empty, empty, bftSlices, tsdcSlicesFine, accelSlices, tsvcSlicesRough, empty, empty, wallSlicesRough},
            {empty, empty, bftSlicesFine, empty, accelSlices, tsvcSlicesRough, dl10SlicesRough, empty, wallSlicesRough},
            {empty, empty, bftSlicesRough, empty, accelSlices, tsvcSlicesFine, dl10Slices, wallSlicesRough, empty},
            {empty, advVelSlicesFine, empty, tsdcSlices, accelSlices, tsvcSlicesRough, empty, wallSlices, empty},
            {velSlicesFine, advVelSlicesFine, bftSlices, empty, empty, tsvcSlicesFine, empty, wallSlices, empty},
            {empty, empty, bftSlices, empty, empty, tsvcSlicesFine, dl10SlicesRough, wallSlices, wallSlicesRough},
            {velSlicesRough, advVelSlicesFine, bftSlices, empty, empty, tsvcSlicesRough, dl10SlicesRough, wallSlices, empty},
            {empty, empty, empty, empty, accelSlices, empty, empty, wallSlicesRough, wallSlicesRough},
            {empty, advVelSlicesRough, bftSlicesRough, tsdcSlices, empty, tsvcSlicesFine, empty, wallSlices, empty},
            {empty, advVelSlicesRough, empty, tsdcSlicesRough, accelSlices, empty, empty, wallSlicesRough, empty},
            {velSlicesFine, advVelSlicesFine, empty, empty, accelSlices, empty, dl10SlicesRough, empty, wallSlicesRough},
            {empty, empty, bftSlicesFine, empty, empty, empty, empty, wallSlicesRough, empty},
            {velSlices, empty, bftSlices, tsdcSlices, accelSlices, empty, empty, empty, empty},
            {velSlicesFine, empty, bftSlicesFine, empty, empty, empty, empty, wallSlicesRough, wallSlicesRough},
            {empty, advVelSlices, bftSlicesRough, empty, empty, empty, dl10Slices, empty, wallSlicesRough},
            {velSlices, advVelSlicesRough, empty, empty, accelSlices, empty, empty, wallSlicesRough, empty},
            {empty, advVelSlicesFine, bftSlices, empty, accelSlices, tsvcSlices, empty, wallSlices, empty},
            {velSlices, advVelSlices, bftSlicesRough, tsdcSlices, accelSlices, empty, empty, empty, empty},
            {empty, advVelSlices, bftSlicesFine, tsdcSlicesRough, empty, empty, empty, empty, wallSlicesRough},
            {empty, empty, bftSlices, empty, empty, tsvcSlices, dl10SlicesFine, empty, wallSlicesRough},
            {empty, empty, bftSlicesFine, tsdcSlices, accelSlices, tsvcSlicesFine, empty, empty, empty},
            {empty, advVelSlices, empty, empty, accelSlices, tsvcSlices, dl10SlicesRough, empty, wallSlicesRough},
            {velSlices, advVelSlices, bftSlicesFine, empty, empty, empty, dl10SlicesRough, empty, empty}
            };  
     
      putBuffersInto(flatTickSets,flattenerTickBuffers);
      return flattenerTickBuffers;
   			
   }

   private static void putBuffersInto(float[][][] sets, ArrayList buffers){
      for(int i = 0; i < sets.length; i++){
         float[][] set = sets[i];
      
         int[] sri = new int[9];
         
         if(set[0] == empty)
            sri[0] = 0;  
         else if(set[0] == velSlicesRough)
            sri[0] = 1;
         else if(set[0] == velSlices)
            sri[0] = 2;
         else if(set[0] == velSlicesFine)
            sri[0] = 3;
            
         if(set[1] == empty)
            sri[1] = 0;  
         else if(set[1] == advVelSlicesRough)
            sri[1] = 1;
         else if(set[1] == advVelSlices)
            sri[1] = 2;
         else if(set[1] == advVelSlicesFine)
            sri[1] = 3;
      		
      		
         if(set[2] == empty)
            sri[2] = 0;  
         else if(set[2] == bftSlicesRough)
            sri[2] = 1;
         else if(set[2] == bftSlices)
            sri[2] = 2;
         else if(set[2] == bftSlicesFine)
            sri[2] = 3;
      	
      	
         if(set[3] == empty)
            sri[3] = 0;  
         else if(set[3] == tsdcSlicesRough)
            sri[3] = 1;
         else if(set[3] == tsdcSlices)
            sri[3] = 2;
         else if(set[3] == tsdcSlicesFine)
            sri[3] = 3;
            
      		
         if(set[4] == empty)
            sri[4] = 0;  
         else if(set[4] == accelSlices)
            sri[4] = 1;
         else if(set[4] == accelSlicesFine)
            sri[4] = 2;
      		
      		
         if(set[5] == empty)
            sri[5] = 0;
         else if(set[5] == tsvcSlicesRough)
            sri[5] = 1;
         else if(set[5] == tsvcSlices)
            sri[5] = 2;
         else if(set[5] == tsvcSlicesFine)
            sri[5] = 3;
            
      		
         if(set[6] == empty)
            sri[6] = 0;  
         else if(set[6] == dl10SlicesRough)
            sri[6] = 1;
         else if(set[6] == dl10Slices)
            sri[6] = 2;
         else if(set[6] == dl10SlicesFine)
            sri[6] = 3;
      		
      		
         if(set[7] == empty)
            sri[7] = 0;  
         else if(set[7] == wallSlicesRough)
            sri[7] = 1;
         else if(set[7] == wallSlices)
            sri[7] = 2;
            
      		
         if(set[8] == empty)
            sri[8] = 0;  
         else if(set[8] == wallSlicesRough)
            sri[8] = 1;
         else if(set[8] == wallSlices)
            sri[8] = 2;	
      
         StatBuffer sb = new StatBuffer(set[0],set[1],set[2],set[3],set[4],set[5],set[6],set[7],set[8],sri);
      	
         buffers.add(sb);
      }
   
   }
   public static int[][] getIndexes(
     float latVel, 
     float advVel,
     float bft, 
     float tsdc, 
     float accel, 
     float tsvc, 
     float dl10, 
     float forwardWall, 
     float reverseWall){
     
   
      int[][] indexes = new int[4][9];
      
   	/// set up indexes
      indexes[1][0] = getIndex(velSlicesRough, latVel);
      indexes[2][0] = getIndex(velSlices, latVel);
      indexes[3][0] = getIndex(velSlicesFine, latVel);
      
      indexes[1][1] = getIndex(advVelSlicesRough, advVel);
      indexes[2][1] = getIndex(advVelSlices, advVel);
      indexes[3][1] = getIndex(advVelSlicesFine, advVel);
   	
      indexes[1][2] = getIndex(bftSlicesRough, bft);
      indexes[2][2] = getIndex(bftSlices, bft);
      indexes[3][2] = getIndex(bftSlicesFine, bft);
      
      indexes[1][3] = getIndex(tsdcSlicesRough, tsdc);
      indexes[2][3] = getIndex(tsdcSlices, tsdc);
      indexes[3][3] = getIndex(tsdcSlicesFine, tsdc);
   	
      indexes[1][4] = getIndex(accelSlices, accel);
      indexes[2][4] = getIndex(accelSlicesFine, accel);
      	
      indexes[1][5] = getIndex(tsvcSlicesRough, tsvc);
      indexes[2][5] = getIndex(tsvcSlices, tsvc);
      indexes[3][5] = getIndex(tsvcSlicesFine, tsvc);
   	
      indexes[1][6] = getIndex(dl10SlicesRough, dl10);
      indexes[2][6] = getIndex(dl10Slices, dl10);
      indexes[3][6] = getIndex(dl10SlicesFine, dl10);
      
      indexes[1][7] = getIndex(wallSlicesRough, forwardWall);
      indexes[2][7] = getIndex(wallSlices, forwardWall);
      
      indexes[1][8] = getIndex(wallSlicesRough, reverseWall);
      indexes[2][8] = getIndex(wallSlices, reverseWall);
   
      return indexes;
     
   }
   public static ArrayList getStats(ArrayList buffers, int[][] indexes){
      ArrayList stats = new ArrayList(buffers.size());
   	
      for(int i = 0, k = buffers.size(); i < k; i++){
         stats.add(((StatBuffer)(buffers.get(i))).getStats(indexes));
      }
   
      return stats;
   }

   private static int getIndex(float[] slices, float value){
      int index = 0;
      while(index < slices.length && value >= slices[index])
         index++;
      return index;
   }

   public static class SingleBuffer{
      public  int binsUsed,hits;
      public       int[] bins;
      public       float weight;
      public       float rollingDepth = 0.7f;
      
      public void addHit(int hitIndex){
         if(bins == null){
            bins = new int[(int)Math.ceil(rollingDepth*2) + 1];
            hits = -1;  
         }
         
         if(binsUsed < bins.length)
            binsUsed+=1;
         
         hits = (hits+1)%bins.length;      
         bins[hits] = hitIndex;
         
      }
   }  

   public static class StatBuffer{  
   
      public       float _weight;
      public       float rollingDepth;
        
      public  SingleBuffer [][][][][][][][][] stats;
   
      public  int[] sri;
   
      public  StatBuffer(
       float[] vSlices,
       float[] aVSlices,
       float[] dSlices,
       float[] tsdcSlices,
       float[] accSlices,
       float[] tsvcSlices,
       float[] dl10Slices,
       float[] fWallSlices,
       float[] rWallSlices, 
       int[] sliceRetrieveIndexes){
      
         stats = new SingleBuffer
            [vSlices.length + 1]
            [aVSlices.length + 1]
            [dSlices.length + 1]
            [tsdcSlices.length + 1]
            [accSlices.length + 1]
            [tsvcSlices.length + 1]
            [dl10Slices.length + 1]
            [fWallSlices.length + 1]
            [rWallSlices.length + 1];
         
         float weight = (vSlices.length + 1)
            *(aVSlices.length + 1)
            *(dSlices.length + 1)
            *(tsdcSlices.length +1)
            *(accSlices.length + 1)
            *(tsvcSlices.length + 1)
            *(dl10Slices.length + 1)
            *(fWallSlices.length + 1)
            *(rWallSlices.length + 1);
      
         if(weight < 2)
            rollingDepth = 3;
         else if(weight < 3)
            rollingDepth = 1;
         // else if(weight < 7)
         // rollingDepth = 1;
         else if(weight < 10)
            rollingDepth = 0.7f;
         else if(weight < 33)
            rollingDepth = 0.5f;
         else if(weight < 100)
            rollingDepth = 0.2f;
         else rollingDepth = 0.1f;
      
      // _weight = 1;
         _weight = weight;
         sri = sliceRetrieveIndexes;
      }
       /*SingleBuffer getStats(float latVel, float advVel, float distance, float tsdc, float accel, float tsvc, float dl10, float forwardWall, float reverseWall){
         SingleBuffer sb = new SingleBuffer();
         sb.bins = stats
            [getIndex(velocitySlices, latVel)]
            [getIndex(advVelocitySlices, advVel)]
            [getIndex(distanceSlices, distance)]
            [getIndex(timeSinceDirChangeSlices, tsdc)]
            [getIndex(accelSlices,accel)]
            [getIndex(timeSinceVelChangeSlices, tsvc)]
            [getIndex(distLast20Slices, dl10)]
            [getIndex(forwardWallSlices,forwardWall)]
            [getIndex(reverseWallSlices,reverseWall)];
         sb.weight = this._weight;
         sb.rollingDepth = this.rollingDepth;
         return sb;
      
      }*/
      public    SingleBuffer getStats(int[][] indexes){
         SingleBuffer sb = stats
            [indexes[sri[0]][0]]
            [indexes[sri[1]][1]]
            [indexes[sri[2]][2]]
            [indexes[sri[3]][3]]
            [indexes[sri[4]][4]]
            [indexes[sri[5]][5]]
            [indexes[sri[6]][6]]
            [indexes[sri[7]][7]]
            [indexes[sri[8]][8]]
            ;
         if(sb == null){
            sb = stats
               [indexes[sri[0]][0]]
               [indexes[sri[1]][1]]
               [indexes[sri[2]][2]]
               [indexes[sri[3]][3]]
               [indexes[sri[4]][4]]
               [indexes[sri[5]][5]]
               [indexes[sri[6]][6]]
               [indexes[sri[7]][7]]
               [indexes[sri[8]][8]] = new SingleBuffer();
               
         	
            sb.weight = this._weight;
            sb.rollingDepth = this.rollingDepth;	 
            sb.hits = -1;
         }
            
       
         return sb;
      
      }
   }
}