package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.InviteMemberVO

interface MemberService {
    fun inviteMember(vo: InviteMemberVO): ApiResponse
}