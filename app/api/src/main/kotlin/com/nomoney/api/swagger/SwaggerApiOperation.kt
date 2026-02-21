package com.nomoney.api.swagger

object SwaggerApiOperation {

    object Auth {
        const val ISSUE_TOKEN_SUMMARY = "토큰 발급"
        const val ISSUE_TOKEN_DESCRIPTION = "사용자의 액세스 토큰과 리프레시 토큰을 발급합니다 (임시 API)"
        const val REFRESH_TOKEN_SUMMARY = "토큰 갱신"
        const val REFRESH_TOKEN_DESCRIPTION = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다"
        const val GOOGLE_SOCIAL_LOGIN_SUMMARY = "구글 소셜 로그인"
        const val GOOGLE_SOCIAL_LOGIN_DESCRIPTION = "구글 OAuth 인증 코드를 사용하여 로그인합니다. 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키로 설정하고 프론트엔드 URL로 리다이렉트합니다."
        const val REFRESH_TOKEN_WITH_COOKIE_SUMMARY = "쿠키 기반 토큰 갱신"
        const val REFRESH_TOKEN_WITH_COOKIE_DESCRIPTION = "HttpOnly 쿠키에 저장된 리프레시 토큰을 사용하여 액세스 토큰과 리프레시 토큰을 갱신합니다."
    }

    object MeetingVote {
        const val GET_MEETING_INFO_SUMMARY = "모임 정보 조회"
        const val GET_MEETING_INFO_DESCRIPTION = "모임 ID로 모임 정보와 참여자들의 투표 현황을 조회합니다"
        const val GET_MEETING_LIST_SUMMARY = "모임 목록 조회"
        const val GET_MEETING_LIST_DESCRIPTION = "모든 모임의 ID, 제목, 주최자를 조회합니다"
        const val GET_IN_PROGRESS_DASHBOARD_SUMMARY = "주최자 진행중 모임 대시보드 조회"
        const val GET_IN_PROGRESS_DASHBOARD_DESCRIPTION = "주최자 기준 진행중(VOTING) 모임 목록과 요약 정보를 조회합니다."
        const val GET_CONFIRMED_DASHBOARD_SUMMARY = "주최자 확정 모임 대시보드 조회"
        const val GET_CONFIRMED_DASHBOARD_DESCRIPTION = "주최자 기준 확정(CONFIRMED) 모임 목록과 요약 정보를 조회합니다."
        const val GET_FINALIZE_PREVIEW_SUMMARY = "모임 확정 후보 조회"
        const val GET_FINALIZE_PREVIEW_DESCRIPTION = "모임 확정 시 필요한 최다 득표 날짜 후보(날짜/득표수/투표자)를 조회합니다."
        const val CREATE_MEETING_SUMMARY = "모임 생성"
        const val CREATE_MEETING_DESCRIPTION = "새로운 모임을 생성하고 고유 ID를 발급합니다"
        const val SAVE_MEETING_MEMO_SUMMARY = "주최자 메모 저장"
        const val SAVE_MEETING_MEMO_DESCRIPTION = "주최자가 모임 메모를 저장합니다. 메모는 200자까지 가능합니다."
        const val UPDATE_MEETING_SUMMARY = "모임 수정"
        const val UPDATE_MEETING_DESCRIPTION = "모임 제목, 최대 인원, 후보 날짜, 삭제할 참여자를 반영해 모임을 수정합니다."
        const val CHECK_DUPLICATE_NAME_SUMMARY = "참여자 이름 중복 확인"
        const val CHECK_DUPLICATE_NAME_DESCRIPTION = "모임에 동일한 이름의 참여자가 있는지 확인합니다"
        const val CREATE_VOTE_SUMMARY = "투표 생성"
        const val CREATE_VOTE_DESCRIPTION = "모임에 대한 투표를 생성합니다. 중복된 이름으로 투표할 경우 에러가 발생합니다."
        const val UPDATE_VOTE_SUMMARY = "투표 수정"
        const val UPDATE_VOTE_DESCRIPTION = "기존 투표 내용을 수정합니다."
        const val FINALIZE_MEETING_SUMMARY = "모임 확정"
        const val FINALIZE_MEETING_DESCRIPTION = "투표 결과를 바탕으로 최종 날짜를 확정하고 모임 상태를 확정으로 전환합니다. 공동 1위인 경우 finalizedDate를 함께 요청해야 합니다."
        const val CHECK_FINALIZED_DATE_CONFLICT_AND_FINALIZE_SUMMARY = "모임 확정 날짜 충돌 확인 및 자동 확정"
        const val CHECK_FINALIZED_DATE_CONFLICT_AND_FINALIZE_DESCRIPTION = "확정 날짜가 주최자의 다른 확정 모임과 겹치는지 확인합니다. 겹치지 않으면 모임을 즉시 확정하고 false를 반환합니다."
    }
}
