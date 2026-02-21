package com.pizzastore.controller;

import com.pizzastore.repository.PromotionsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PromotionsController {

    private final PromotionsRepository promotionsRepository;

    public PromotionsController(PromotionsRepository promotionsRepository) {
        this.promotionsRepository = promotionsRepository;
    }

    @GetMapping("/promotions")
    public List<Map<String, Object>> getPromotions() {
        try {
            return promotionsRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load promotions from the database. Make sure a 'promotions' table exists and the app has access.",
                    e
            );
        }
    }

    @PostMapping("/promotions")
    public ResponseEntity<Map<String, Object>> createPromotion(@RequestBody Map<String, Object> body) {
        return savePromotionInternal(null, body, true);
    }

    @PutMapping("/promotions/{promotionId}")
    public ResponseEntity<Map<String, Object>> updatePromotion(
            @PathVariable long promotionId,
            @RequestBody Map<String, Object> body
    ) {
        return savePromotionInternal(promotionId, body, false);
    }

    private ResponseEntity<Map<String, Object>> savePromotionInternal(Long promotionId, Map<String, Object> body, boolean creating) {
        Map<String, Object> resp = new HashMap<>();

        try {
            String code = toTrimmed(body.get("code"));
            String discountRaw = toTrimmed(body.get("discount_value"));
            String promotionDesc = toTrimmed(body.get("promotion_desc"));
            String promotionSummary = toTrimmed(body.get("promotion_summary"));
            String expDtRaw = toTrimmed(body.get("exp_dt"));
            String minOrderAmtRaw = toTrimmed(body.get("min_order_amt"));

            if (code.isEmpty() || discountRaw.isEmpty() || promotionDesc.isEmpty()
                    || promotionSummary.isEmpty() || expDtRaw.isEmpty() || minOrderAmtRaw.isEmpty()) {
                resp.put("success", false);
                resp.put("message", "All fields are required.");
                return ResponseEntity.badRequest().body(resp);
            }

            BigDecimal discountValue;
            try {
                discountValue = new BigDecimal(discountRaw);
            } catch (NumberFormatException nfe) {
                resp.put("success", false);
                resp.put("message", "Discount must be numeric.");
                return ResponseEntity.badRequest().body(resp);
            }

            BigDecimal minOrderAmt;
            try {
                minOrderAmt = new BigDecimal(minOrderAmtRaw);
            } catch (NumberFormatException nfe) {
                resp.put("success", false);
                resp.put("message", "Minimum order amount must be numeric.");
                return ResponseEntity.badRequest().body(resp);
            }

            Date expDt;
            try {
                expDt = Date.valueOf(expDtRaw); // expects YYYY-MM-DD
            } catch (IllegalArgumentException ex) {
                resp.put("success", false);
                resp.put("message", "Expiration date must be in YYYY-MM-DD format.");
                return ResponseEntity.badRequest().body(resp);
            }

            boolean ok;
            if (creating) {
                ok = promotionsRepository.createPromotion(code, discountValue, promotionDesc, promotionSummary, expDt, minOrderAmt);
            } else {
                ok = promotionsRepository.updatePromotion(promotionId, code, discountValue, promotionDesc, promotionSummary, expDt, minOrderAmt);
            }

            if (ok) {
                resp.put("success", true);
                resp.put("message", creating ? "Promotion added successfully." : "Promotion updated successfully.");
                return ResponseEntity.ok(resp);
            } else {
                resp.put("success", false);
                resp.put("message", creating ? "Failed to add promotion." : "Update failed. Promotion not found or no changes saved.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", creating ? "Failed to add promotion due to a server error." : "Update failed due to a server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    private String toTrimmed(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    @DeleteMapping("/promotions/{promotionId}")
    public ResponseEntity<Map<String, Object>> deletePromotion(@PathVariable long promotionId) {
        Map<String, Object> resp = new HashMap<>();

        try {
            boolean ok = promotionsRepository.deletePromotion(promotionId);
            if (ok) {
                resp.put("success", true);
                resp.put("message", "Promotion deleted successfully.");
                return ResponseEntity.ok(resp);
            } else {
                resp.put("success", false);
                resp.put("message", "Delete failed. Promotion not found.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Delete failed due to a server error.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}
