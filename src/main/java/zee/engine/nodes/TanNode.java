
package zee.engine.nodes;

import zee.engine.domain.DomainInterface;

public class TanNode extends MathNode {

   public TanNode(Object id) {
      super(id);
   }
 
   @Override
   public double[] performCalculation(DomainInterface domain) {
      double[] v1 = getChild(0).evaluate(domain);
      for(int i=0; i < v1.length; i++)
         v1[i] = Math.tan(v1[i]);
      return v1;
   }

   
}
