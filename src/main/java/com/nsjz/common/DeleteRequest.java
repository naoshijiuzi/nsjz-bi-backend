package com.nsjz.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 郭春燕
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
