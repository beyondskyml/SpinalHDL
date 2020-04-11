/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package spinal.sim.vpi;

public class VectorInt8 extends java.util.AbstractList<Byte> implements java.util.RandomAccess {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected VectorInt8(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(VectorInt8 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        JNISharedMemIfaceJNI.delete_VectorInt8(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public VectorInt8(byte[] initialElements) {
    this();
    reserve(initialElements.length);

    for (byte element : initialElements) {
      add(element);
    }
  }

  public VectorInt8(Iterable<Byte> initialElements) {
    this();
    for (byte element : initialElements) {
      add(element);
    }
  }

  public Byte get(int index) {
    return doGet(index);
  }

  public Byte set(int index, Byte e) {
    return doSet(index, e);
  }

  public boolean add(Byte e) {
    modCount++;
    doAdd(e);
    return true;
  }

  public void add(int index, Byte e) {
    modCount++;
    doAdd(index, e);
  }

  public Byte remove(int index) {
    modCount++;
    return doRemove(index);
  }

  protected void removeRange(int fromIndex, int toIndex) {
    modCount++;
    doRemoveRange(fromIndex, toIndex);
  }

  public int size() {
    return doSize();
  }

  public VectorInt8() {
    this(JNISharedMemIfaceJNI.new_VectorInt8__SWIG_0(), true);
  }

  public VectorInt8(VectorInt8 other) {
    this(JNISharedMemIfaceJNI.new_VectorInt8__SWIG_1(VectorInt8.getCPtr(other), other), true);
  }

  public long capacity() {
    return JNISharedMemIfaceJNI.VectorInt8_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    JNISharedMemIfaceJNI.VectorInt8_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return JNISharedMemIfaceJNI.VectorInt8_isEmpty(swigCPtr, this);
  }

  public void clear() {
    JNISharedMemIfaceJNI.VectorInt8_clear(swigCPtr, this);
  }

  public VectorInt8(int count, byte value) {
    this(JNISharedMemIfaceJNI.new_VectorInt8__SWIG_2(count, value), true);
  }

  private int doSize() {
    return JNISharedMemIfaceJNI.VectorInt8_doSize(swigCPtr, this);
  }

  private void doAdd(byte x) {
    JNISharedMemIfaceJNI.VectorInt8_doAdd__SWIG_0(swigCPtr, this, x);
  }

  private void doAdd(int index, byte x) {
    JNISharedMemIfaceJNI.VectorInt8_doAdd__SWIG_1(swigCPtr, this, index, x);
  }

  private byte doRemove(int index) {
    return JNISharedMemIfaceJNI.VectorInt8_doRemove(swigCPtr, this, index);
  }

  private byte doGet(int index) {
    return JNISharedMemIfaceJNI.VectorInt8_doGet(swigCPtr, this, index);
  }

  private byte doSet(int index, byte val) {
    return JNISharedMemIfaceJNI.VectorInt8_doSet(swigCPtr, this, index, val);
  }

  private void doRemoveRange(int fromIndex, int toIndex) {
    JNISharedMemIfaceJNI.VectorInt8_doRemoveRange(swigCPtr, this, fromIndex, toIndex);
  }

}