/*
 * Copyright 2012-2016 the Flamingo Community.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exem.flamingo.shared.core.exception;

import org.exem.flamingo.shared.util.DateUtils;
import org.apache.commons.lang.math.RandomUtils;

/**
 * 기본 서비스 요청의 에러 발생시 던지는 예외.
 *
 * @author Byoung Gon, Kim
 * @since 0.1
 */
public class ServiceException extends RuntimeException {

    /**
     * Serialization UID
     */
    private static final long serialVersionUID = 1;

    /**
     * Exception ID
     */
    private String exceptionId = DateUtils.getCurrentDateTime() + "_" + RandomUtils.nextLong();

    /**
     * 예외 메시지
     */
    private String message = null;

    /**
     * 종료 코드 (FIXME 필요한지 확인)
     */
    private int exitCode;

    /**
     * 예외 코드
     */
    private int code;

    /**
     * 가장 최근 로그 (FIXME 필요한지 확인)
     */
    private String recentLog;

    /**
     * 기본 생성자.
     */
    public ServiceException() {
        super();
    }

    /**
     * 기본 생성자.
     *
     * @param message 예외 메시지
     */
    public ServiceException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * 기본 생성자.
     *
     * @param message 예외 메시지
     * @param cause   예외 원인
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    /**
     * 기본 생성자.
     *
     * @param message 예외 메시지
     * @param code    에러 코드
     * @param cause   예외 원인
     */
    public ServiceException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 기본 생성자.
     *
     * @param cause 예외 원인
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * 기본 생성자.
     *
     * @param exitCode  프로세스 종료 코드
     * @param recentLog 최근 실행 로그
     */
    public ServiceException(int exitCode, String recentLog) {
        this.exitCode = exitCode;
        this.message = String.valueOf(exitCode);
        this.recentLog = recentLog;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getRecentLog() {
        return recentLog;
    }

    /**
     * Root Cause를 반환한다.
     *
     * @return Root Cause
     */
    public Throwable getRootCause() {
        return super.getCause();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getExceptionId() {
        return exceptionId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setRecentLog(String recentLog) {
        this.recentLog = recentLog;
    }

    @Override
    public String toString() {
        return message;
    }
}
