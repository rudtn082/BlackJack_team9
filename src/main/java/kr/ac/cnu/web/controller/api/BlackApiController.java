package kr.ac.cnu.web.controller.api;

import kr.ac.cnu.web.exceptions.NoLoginException;
import kr.ac.cnu.web.exceptions.NoUserException;
import kr.ac.cnu.web.games.blackjack.GameRoom;
import kr.ac.cnu.web.model.User;
import kr.ac.cnu.web.repository.UserRepository;
import kr.ac.cnu.web.service.BlackjackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Created by rokim on 2018. 5. 21..
 */
@RestController
@RequestMapping("/api/black-jack")
@CrossOrigin
public class BlackApiController {
    @Autowired
    private BlackjackService blackjackService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public User login(@RequestBody String name) {
        return userRepository.findById(name).orElseThrow(() -> new NoUserException());
    }

    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public User singup(@RequestBody String name) {
        // TODO check already used name
        Optional<User> userOptional = userRepository.findById(name);
        if (userOptional.isPresent()) {
            throw new RuntimeException();

        }

        // TODO new user
        User user = new User(name, 50000);

        // TODO save in repository
        return userRepository.save(user);
    }

    @PostMapping("/rooms/{roomId}/double_down")
    public GameRoom double_down(@RequestHeader("name") String name, @PathVariable String roomId, @RequestBody long betMoney) {
        User user = this.getUserFromSession(name);

        GameRoom gameRoom = blackjackService.double_down(roomId, user, betMoney);

        // BugFix3
        // 수행된 게임룸에서 플레이어의 balance를 가져옴.
        user.setAccount(gameRoom.getPlayerList().get(name).getBalance());

        // 그 값을 저장 userRepository 업데이트
        userRepository.save(user);

        return gameRoom;
    }



    @PostMapping("/rooms")
    public GameRoom createRoom(@RequestHeader("name") String name) {
        User user = this.getUserFromSession(name);

        return blackjackService.createGameRoom(user);
    }

    @PostMapping(value = "/rooms/{roomId}/bet", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GameRoom bet(@RequestHeader("name") String name, @PathVariable String roomId, @RequestBody long betMoney) {
        User user = this.getUserFromSession(name);

        // 기능추가 8
        // 베팅금액이 10000을 넘어갈 수 없도록 확인
        if(betMoney > 10000){
            throw new RuntimeException();
        }

        GameRoom gameRoom = blackjackService.bet(roomId, user, betMoney);

        // BugFix3
        // 수행된 게임룸에서 플레이어의 balance를 가져옴.
        user.setAccount(gameRoom.getPlayerList().get(name).getBalance());

        // 그 값을 저장 userRepository 업데이트
        userRepository.save(user);


        return gameRoom;
    }

    @PostMapping("/rooms/{roomId}/hit")
    public GameRoom hit(@RequestHeader("name") String name, @PathVariable String roomId) {
        User user = this.getUserFromSession(name);

        GameRoom gameRoom = blackjackService.hit(roomId, user);

        // BugFix3
        // 수행된 게임룸에서 플레이어의 balance를 가져옴.
        user.setAccount(gameRoom.getPlayerList().get(name).getBalance());

        // 그 값을 저장 userRepository 업데이트
        userRepository.save(user);


        return gameRoom;
    }

    @PostMapping("/rooms/{roomId}/stand")
    public GameRoom stand(@RequestHeader("name") String name, @PathVariable String roomId) {
        User user = this.getUserFromSession(name);


        GameRoom gameRoom = blackjackService.stand(roomId, user);

        // BugFix3
        // 수행된 게임룸에서 플레이어의 balance를 가져옴.
        user.setAccount(gameRoom.getPlayerList().get(name).getBalance());

        // 그 값을 저장 userRepository 업데이트
        userRepository.save(user);

        return gameRoom;
    }

    @GetMapping("/rooms/{roomId}")
    public GameRoom getGameRoomData(@PathVariable String roomId) {
        return blackjackService.getGameRoom(roomId);
    }

    @GetMapping("/getName")
    public String getName() {
        List<User> list = userRepository.findAll();

        String text = "";
        Collections.sort(list, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {

                if (o1.getAccount() > o2.getAccount()) {
                    return -1;
                } else if (o1.getAccount() < o2.getAccount()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        int Size = 0;
        if (Size >=6){
            Size = 6;
        } else {
            Size = list.size();
        }
        for (int i = 0; i < Size; i++) {
            text += (i+1)+ "."+ list.get(i).getName() + " :  " + list.get(i).getAccount() +"<br>";

        }
        System.out.println(text);

        return text;
    }





    private User getUserFromSession(String name) {
        return userRepository.findById(name).orElseThrow(() -> new NoLoginException());
    }
}
