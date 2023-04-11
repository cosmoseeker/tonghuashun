package org.qianyuxiang.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class IdentityEntity implements Serializable {
    private static final long serialVersionUID = 66331428641999302L;
    /**
     * 实体标识
     */
    private String identity;
    private BigDecimal value;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
