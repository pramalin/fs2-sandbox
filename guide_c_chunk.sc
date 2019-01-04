object guide_b_chunk {
  import fs2.Stream

  /*
	Chunks
		FS2 streams are chunked internally for performance. You can construct an individual stream
		chunk using Stream.chunk, which accepts an fs2.Chunk and lots of functions in the library
	 	are chunk-aware and/or try to preserve chunks when possible. A Chunk is a strict, finite
	  sequence of values that supports efficient indexed based lookup of elements.
	*/
  import fs2.Chunk

  val s1c = Stream.chunk(Chunk.doubles(Array(1.0, 2.0, 3.0)))
                                                  //> s1c  : fs2.Stream[[x]fs2.Pure[x],Double] = Stream(..)

  s1c.mapChunks { ds =>
    val doubles = ds.toDoubles
    /* do things unboxed using doubles.{values,size} */
    doubles
  }                                               //> res0: fs2.Stream[[x]fs2.Pure[x],Double] = Stream(..)

  /*
	Note: FS2 used to provide an alternative to Chunk which was potentially infinite and
 	supported fusion of arbitrary operations. This type was called Segment. In FS2 0.10.x,
  Segment played a large role in the core design. In FS2 1.0, Segment was completely removed,
  as chunk based algorithms are often faster than their segment based equivalents and
   almost always significantly simpler.
	*/
	
}