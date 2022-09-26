package com.mayo.community.service;

import com.mayo.community.entity.User;
import com.mayo.community.util.CommunityConstant;
import com.mayo.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);

                boolean isMember = operations.opsForZSet().score(followeeKey, entityId) == null ? false : true;
                operations.multi();
                if(isMember) {
                    operations.opsForZSet().remove(followeeKey, entityId);
                    operations.opsForZSet().remove(followerKey, userId);
                }else {
                    operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                    operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                }
                return operations.exec();
            }
        });
    }

//    public void unfollow(int userId, int entityType, int entityId){
//        redisTemplate.execute(new SessionCallback() {
//            @Override
//            public Object execute(RedisOperations operations) throws DataAccessException {
//                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
//                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
//
//                operations.multi();
//                operations.opsForZSet().remove(followeeKey, entityId);
//                operations.opsForZSet().remove(followerKey, userId);
//                return operations.exec();
//            }
//        });
//    }

    // 查询关注的实体数量
    public long findFolloweeCount(int userId, int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某用户的关注
    public List<Map<String, Object>> findFollowee(int loginUserId, int userId, int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if(targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double followTime = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(followTime.longValue()));
            map.put("hasFollowed", hasFollowed(loginUserId, ENTITY_TYPE_USER, targetId));
            list.add(map);
        }
        return list;
    }

    // 查询某用户的粉丝
    public List<Map<String, Object>> findFollowers(int loginUserId, int userId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if(targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer targetId : targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user", user);
            Double followTime = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(followTime.longValue()));
            map.put("hasFollowed", hasFollowed(loginUserId, ENTITY_TYPE_USER, targetId));
            list.add(map);
        }
        return list;
    }
}
