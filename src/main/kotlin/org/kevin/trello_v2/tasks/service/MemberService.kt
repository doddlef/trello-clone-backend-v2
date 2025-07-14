package org.kevin.trello_v2.tasks.service

import org.kevin.trello_v2.framework.response.ApiResponse
import org.kevin.trello_v2.tasks.service.vo.RemoveMemberVO
import org.kevin.trello_v2.tasks.service.vo.InviteMemberVO
import org.kevin.trello_v2.tasks.service.vo.UpdateMemberVO

/**
 * for a membership to exist, the user must has an active membership in the board.
 */
interface MemberService {
    /**
     * Invites a member to a board.
     * The target member must not already be an member of the board.
     *
     * @param vo The value object containing the details of the member to invite.
     * @return An ApiResponse indicating the result of the operation.
     */
    fun inviteMember(vo: InviteMemberVO): ApiResponse

    /**
     * update current member's role in the board.
     */
    fun updateMember(vo: UpdateMemberVO): ApiResponse

    /**
     * remove current member from the board.
     * The target member must has an active membership in the board.
     */
    fun removeMember(vo: RemoveMemberVO): ApiResponse
}