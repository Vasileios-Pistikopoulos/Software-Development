package _5336_4701_5281.swdeproj.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationTest {

    @Test
    void getId() {
        Evaluation eval = new Evaluation();
        eval.setId(42L);
        assertEquals(42L, eval.getId());
    }

    @Test
    void setId() {
        Evaluation eval = new Evaluation();
        eval.setId(99L);
        assertEquals(99L, eval.getId());
    }

    @Test
    void getDate() {
        Evaluation eval = new Evaluation();
        LocalDate date = LocalDate.of(2024, 6, 1);
        eval.setDate(date);
        assertEquals(date, eval.getDate());
    }

    @Test
    void setDate() {
        Evaluation eval = new Evaluation();
        LocalDate date = LocalDate.now();
        eval.setDate(date);
        assertEquals(date, eval.getDate());
    }

    @Test
    void getMotivationRating() {
        Evaluation eval = new Evaluation();
        eval.setMotivationRating(4);
        assertEquals(4, eval.getMotivationRating());
    }

    @Test
    void setMotivationRating() {
        Evaluation eval = new Evaluation();
        eval.setMotivationRating(2);
        assertEquals(2, eval.getMotivationRating());
    }

    @Test
    void getEffectivenessRating() {
        Evaluation eval = new Evaluation();
        eval.setEffectivenessRating(5);
        assertEquals(5, eval.getEffectivenessRating());
    }

    @Test
    void setEffectivenessRating() {
        Evaluation eval = new Evaluation();
        eval.setEffectivenessRating(3);
        assertEquals(3, eval.getEffectivenessRating());
    }

    @Test
    void getEfficiencyRating() {
        Evaluation eval = new Evaluation();
        eval.setEfficiencyRating(1);
        assertEquals(1, eval.getEfficiencyRating());
    }

    @Test
    void setEfficiencyRating() {
        Evaluation eval = new Evaluation();
        eval.setEfficiencyRating(5);
        assertEquals(5, eval.getEfficiencyRating());
    }

    @Test
    void getFacilitiesRating() {
        Evaluation eval = new Evaluation();
        eval.setFacilitiesRating(3);
        assertEquals(3, eval.getFacilitiesRating());
    }

    @Test
    void setFacilitiesRating() {
        Evaluation eval = new Evaluation();
        eval.setFacilitiesRating(2);
        assertEquals(2, eval.getFacilitiesRating());
    }

    @Test
    void getGuidanceRating() {
        Evaluation eval = new Evaluation();
        eval.setGuidanceRating(4);
        assertEquals(4, eval.getGuidanceRating());
    }

    @Test
    void setGuidanceRating() {
        Evaluation eval = new Evaluation();
        eval.setGuidanceRating(1);
        assertEquals(1, eval.getGuidanceRating());
    }

    @Test
    void getComments() {
        Evaluation eval = new Evaluation();
        eval.setComments("Great progress");
        assertEquals("Great progress", eval.getComments());
    }

    @Test
    void setComments() {
        Evaluation eval = new Evaluation();
        eval.setComments("Needs improvement");
        assertEquals("Needs improvement", eval.getComments());
    }

    @Test
    void getTraineeship() {
        Evaluation eval = new Evaluation();
        Traineeship t = new Traineeship();
        eval.setTraineeship(t);
        assertEquals(t, eval.getTraineeship());
    }

    @Test
    void setTraineeship() {
        Evaluation eval = new Evaluation();
        Traineeship t = new Traineeship();
        eval.setTraineeship(t);
        assertEquals(t, eval.getTraineeship());
    }

    @Test
    void getEvaluator() {
        Evaluation eval = new Evaluation();
        User u = new User();
        eval.setEvaluator(u);
        assertEquals(u, eval.getEvaluator());
    }

    @Test
    void setEvaluator() {
        Evaluation eval = new Evaluation();
        User u = new User();
        eval.setEvaluator(u);
        assertEquals(u, eval.getEvaluator());
    }

    @Test
    void getEvaluatorType() {
        Evaluation eval = new Evaluation();
        eval.setEvaluatorType(Evaluation.EvaluatorType.COMPANY);
        assertEquals(Evaluation.EvaluatorType.COMPANY, eval.getEvaluatorType());
    }

    @Test
    void setEvaluatorType() {
        Evaluation eval = new Evaluation();
        eval.setEvaluatorType(Evaluation.EvaluatorType.PROFESSOR);
        assertEquals(Evaluation.EvaluatorType.PROFESSOR, eval.getEvaluatorType());
    }

    @Test
    void testEquals() {
        Evaluation eval1 = new Evaluation();
        Evaluation eval2 = new Evaluation();
        eval1.setId(1L);
        eval2.setId(1L);
        assertEquals(eval1, eval2);

        eval2.setId(2L);
        assertNotEquals(eval1, eval2);

        assertNotEquals(eval1, null);
        assertNotEquals(eval1, new Object());
        assertEquals(eval1, eval1);
    }

    @Test
    void testHashCode() {
        Evaluation eval1 = new Evaluation();
        Evaluation eval2 = new Evaluation();
        eval1.setId(5L);
        eval2.setId(5L);
        assertEquals(eval1.hashCode(), eval2.hashCode());

        eval2.setId(6L);
        assertNotEquals(eval1.hashCode(), eval2.hashCode());
    }
}