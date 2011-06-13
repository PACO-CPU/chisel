// author: jonathan bachrach
package Chisel {

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Queue
import scala.collection.mutable.Stack
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import java.lang.reflect.Modifier._;
import java.io.File;

import scala.math.log;
import scala.math.abs;
import scala.math.ceil;
import scala.math.max;
import scala.math.min;
import Node._;
import Wire._;
import Lit._;
import Op._;
import Reg._;
import Component._;
import Bundle._;
import IOdir._;

object Lit {
  implicit def intToLit (x: Int) = Lit(x);
  def sizeof(x: Int): Int = { 
    val y = max(1, abs(x)).toDouble;
    val res = max(1, (ceil(log(y+1)/log(2.0))).toInt);
    // println("SIZEOF " + y + " LOG2 " + (log(y)/log(2.0)) + " IS " + res);
    res
  }
  val hexNibbles = "0123456789abcdef";
  def toHexNibble(x: String, off: Int): Char = {
    var res = 0;
    // println("OFF = " + off);
    for (i <- 0 until 4) {
      val idx = off+i;
      val c   = if (idx < 0) '0' else x(idx);
      res     = 2 * res + (if (c == '1') 1 else 0);
    }
    hexNibbles(res)
  }
  val pads = Vector(0, 3, 2, 1);
  def toHex(x: String): String = {
    var res = "";
    val numNibbles = (x.length-1) / 4 + 1;
    val pad = pads(x.length % 4);
    // println("X = " + x + " NN = " + numNibbles + " PAD = " + pad);
    for (i <- 0 until numNibbles) {
      res += toHexNibble(x, i*4 - pad);
    }
    res
  }
  def toLitVal(x: String): Int = {
    var res = 0;
    for (c <- x.substring(2, x.length)) 
      res = res * 16 + c.asDigit;
    res
  }

  def toLitVal(x: String, shamt: Int): Int = {
    var res = 0;
    for(c <- x)
      res = res * shamt + c.asDigit;
    res
  }

  def parseLit(x: String): (String, String, Int) = {
    var bits = "";
    var mask = "";
    var width = 0;
    for (d <- x) {
      if (d != '_') {
        width += 1;
        mask   = mask + (if (d == '?') "0" else "1");
        bits   = bits + (if (d == '?') "0" else d.toString);
      }
    }
    (bits, mask, width)
  }
  def stringToVal(base: Char, x: String): Int = {
    if(base == 'x')
      toLitVal(x);
    else if(base == 'd')
      x.toInt
    else if(base == 'h')
      toLitVal(x, 16)
    else if(base == 'b')
      toLitVal(x, 2)  
    else
      -1
  }

  def apply(x: Int): Lit = { val res = new Lit(); res.init("0x%x".format(x), sizeof(x)); res }
  def apply(x: Int, width: Int): Lit = { val res = new Lit(); res.init("0x%x".format(x), width); res }
  def apply(x: Long, width: Int): Lit = { val res = new Lit(); res.init("0x%x".format(x), width); res }
  // def apply(n: String): Lit = { 
  //   val (bits, mask, width) = parseLit(n);  apply(n, width);
  // }
  def apply(n: String, width: Int): Lit = 
    apply(width, n(0), n.substring(1, n.length));
  def apply(width: Int, base: Char, literal: String): Lit = {
    if (!"dhb".contains(base)) throw new IllegalArgumentException("invalid base");
    val res = new Lit();
    res.init(literal, width); res.base = base;
    if (base == 'b') {res.isZ = literal.contains('?'); res.isBinary = true;}
    res
  }
}
class Lit extends Node {
  //implicit def intToLit (x: Int) = Lit(x);
  var isZ = false;
  var isBinary = false;
  var base = 'x';
  // override def toString: String = "LIT(" + name + ")"
  override def findNodes(depth: Int, c: Component): Unit = { }
  override def value: Int = stringToVal(base, name);
  override def maxNum = value;
  override def minNum = value;
  override def isLit = true;
  override def toString: String = name;
  override def emitDecC: String = "";
  override def emitRefC: String = 
    if (isBinary) { 
      var (bits, mask, swidth) = parseLit(name);
      var bwidth = if(base == 'b') width else swidth;
      if (isZ) {
        ("LITZ<" + bwidth + ">(0x" + toHex(bits) + ", 0x" + toHex(mask) + ")")
      } else
        ("LIT<" + bwidth + ">(0x" + toHex(bits) + ")")
    } else if(base == 'd' || base == 'x'){
      ("LIT<" + width + ">(" + name + "L)")
    } else
      ("LIT<" + width + ">(0x" + name + "L)")
  

  override def emitDec: String = "";
  override def emitRefV: String = 
    if (width == -1) name 
    else if(isBinary) ("" + width + "'b" + name)
    else if(base == 'x') ("" + width + "'h" + name.substring(2, name.length))
    else if(base == 'd') ("" + width + "'d" + name)
    else if(base == 'h') ("" + width + "'h" + name)
    else "";
  def d (x: Int): Lit = Lit(x, value)
  //def ~(x: String): Lit = Lit(value, x(0), x.substring(1, x.length));
}

}