package wikipedia

object ListMethodsExamples extends App{

  val list1 = List(1,2,3,4,5,6)
  //"par" es para retornar una lista paralela con los mismo elementos de la original
  /*EXPLICACION POR PARTES DEL AGGREGATE
  -- aggregate((0, 0)) QUIERE DECIR  que el tipo de resultado va a ser una tupla, si vamos a la función vemos que es como un
      generic que recibe cualquier cosa y nosotros decidimos que tipo retornar
      por ser una tupla se inicializa con 00 por ser una suma

  x._1 y x._2 son los elementos de la tupla que inicialmente inician en 0, 0
  firma de la funcion es (z: =>S)(seqop: (S, T) => S, combop: (S, S) => S)
  Siendo (z: =>S) el tipo que se desea retornar y (seqop: (S, T) => S, combop: (S, S) => S) se divide el seqo que es una operacion que se
  ejecuta en cada iteración donde su primer elemento se refiere al tipo de resultado y el segundo al elemento de la lista

  en este caso seqop es: (x, y) => (x._1 + y, x._2 + 1) dice que x es la tupla ; Y es el elemento de la lista
  por lo cual a la TUPLA De resultado en la primera posicipon x._1 le sumará en elemento en y
  y a la segunda posición x._2 le sumará 1 en cada iteración

  combop en éste caso es (x,y) => (x._1 + y._1, x._2 + y._2) que indica que por ejemplo si la operación en seqop se ejecutó en un contexto
  paralelo entonces el consenso del resultado final es que por cada tupla generada ejemplo que existan 3 hilos y el cada uno agarra de a 2 elementos de la lista
   original de éste ejemplo que tiene 6 elementos y computaron cada hilo soliguiente:
   hilo1: (0+1, 1)....(1+2, 1) .. return (3,2)
   hilo1: (0+3, 1)....(3+4, 1) .. return (7,2)
   hilo1: (0+5, 1)....(5+6, 1) .. return (11,2)

   siendo elementos  de combop = justamente los resultados de cada hilo, al combinarlos (comboperation) entonces da (21,6)

  */
  val aggregateResult = list1.par.aggregate((0, 0))((x, y) => (x._1 + y, x._2 + 1), (x,y) => (x._1 + y._1, x._2 + y._2))
  System.out.println(aggregateResult)

}
