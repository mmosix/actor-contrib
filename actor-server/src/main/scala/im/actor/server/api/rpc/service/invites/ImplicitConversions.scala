package im.actor.server.api.rpc.service.invites

import im.actor.api.rpc.invites.ApiInviteState
import im.actor.server.model.InviteState

import scala.language.implicitConversions

trait ImplicitConversions {

  implicit def toApiInviteState(i: InviteState): ApiInviteState =
    ApiInviteState(i.inviteeEmail, i.inviteeName, Some(i.inviterUserId), i.teamId)

  implicit def toApiInviteStates(seq: IndexedSeq[InviteState]): IndexedSeq[ApiInviteState] =
    seq map toApiInviteState

}
