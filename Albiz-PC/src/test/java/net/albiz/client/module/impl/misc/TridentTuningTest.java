package net.albiz.client.module.impl.misc;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TridentTuningTest {
 @Test void concurrentBoostsAreNotMultiplied(){assertEquals(3,TridentTuning.combine(2,3));}
 @Test void defaultsPreserveExistingBoost(){assertEquals(2,TridentTuning.combine(2,1));assertEquals(1,TridentTuning.combine(1,1));assertEquals(.5,TridentTuning.combine(.5,1));}
 @Test void malformedValuesAreFiniteAndBounded(){assertEquals(1,TridentTuning.combine(Double.NaN,Double.POSITIVE_INFINITY));assertEquals(10,TridentTuning.combine(999,-1));}
}
