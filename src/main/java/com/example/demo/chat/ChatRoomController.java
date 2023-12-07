package com.example.demo.chat;

import com.example.demo.member.Member;
import com.example.demo.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/chat")
public class ChatRoomController {

    private final com.example.demo.chat.ChatRoomRepository chatRoomRepository;
    private final MemberService memberService;
    private final ChatRoomService chatRoomService;
    private final ChatRoomDao chatRoomDao;
    private final ChatMessageService chatMessageService;

    // 채팅 리스트 화면 view
    @GetMapping("/room")
    public String rooms(Model map) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member loginMember = (Member) authentication.getPrincipal();
        ArrayList<Member> mlist = memberService.getByIdNot(loginMember.getId());

        map.addAttribute("mlist", mlist);

        return "chat/room";
    }

    // 참여한 채팅방 반환
    @GetMapping("/rooms/{participantId}")
    @ResponseBody
    public List<ChatRoom> getByParticipantId(@PathVariable("participantId") long participantId) {
        return chatRoomService.getByParticipantId(participantId);
    }

    // 모든 채팅방 목록 반환
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        return chatRoomRepository.findAllRoom();
    }

    // 채팅방 생성
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam("name") String roomName, @RequestParam("participants") Set<Member> participants) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member loginMember = (Member) authentication.getPrincipal();

        participants.add(loginMember);

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .roomName(roomName)
                .participants(participants)
                .build();

        return chatRoomService.create(chatRoom);
    }

    // 채팅방 입장 화면 view
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable int roomId) {
        ChatRoom chatRoom = chatRoomService.getById(roomId);
        List<ChatMessage> clist = chatMessageService.getByRoomId(roomId);
        List<Member> nlist = memberService.getNonParticipantsMembers(roomId);

        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("clist", clist);
        model.addAttribute("nlist", nlist);
        return "chat/roomdetail";
    }

    // 특정 채팅방 조회
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable int roomId) {
        return chatRoomDao.getById(roomId);
    }

    // 채팅방 나가기
    @PostMapping("/room/out/{roomId}")
    @ResponseBody
    public boolean roomOut(@PathVariable int roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member loginMember = (Member) authentication.getPrincipal();
        boolean flag = true;

        ChatRoom chatRoom = chatRoomService.getById(roomId);
        chatRoom.getParticipants().removeIf(member -> member.getId().equals(loginMember.getId()));

        chatRoomService.edit(chatRoom);

        return flag;
    }

    // 채팅방 초대
    @PostMapping("/room/invite/{roomId}")
    @ResponseBody
    public boolean roomOut(@PathVariable int roomId, @RequestParam("invitation") Set<Member> invitation) {
        boolean flag = true;
        System.out.println("invitation = " + invitation);
        ChatRoom chatRoom = chatRoomService.getById(roomId);

//        System.out.println("chatRoom = " + chatRoom.getParticipants());


        Set<Member> s = new HashSet<>();
        s.addAll(chatRoom.getParticipants());
        s.addAll(invitation);
        chatRoom.setParticipants(s);

//        System.out.println("roomId = " + roomId);
//        System.out.println("s = " + s);
//        System.out.println("chatRoom = " + chatRoom.getParticipants());


        chatRoomService.edit(chatRoom);

        return flag;
    }
}