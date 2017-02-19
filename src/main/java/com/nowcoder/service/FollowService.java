package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by rainday on 16/8/11.
 */
@Service
public class FollowService {
   @Autowired
   private JedisAdapter jedisAdapter;

    /**
     * 用户关注了某个实体,可以关注问题,关注用户,关注评论等任何实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean follow(int userId, int entityType, int entityId) {
        /*String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Date date = new Date();
        // 实体的粉丝增加当前用户
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx = jedisAdapter.multi(jedis);
        tx.zadd(followerKey, date.getTime(), String.valueOf(userId));
        // 当前用户对这类实体关注+1
        tx.zadd(followeeKey, date.getTime(), String.valueOf(entityId));
        List<Object> ret = jedisAdapter.exec(tx, jedis);
        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;*/

        //获取follower followeekey
        String follower = RedisKeyUtil.getFollowerKey(entityType,entityId);
        String follwee = RedisKeyUtil.getFolloweeKey(userId,entityType);
        //设置日期 因为使用的是Zset 所以要加权 时间就是个加权的好东西
        Date date = new Date();
        //通过jedisAdapter获取事务
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx =  jedisAdapter.multi(jedis);
        // 被关注的实体的粉丝增加当前用户
        tx.zadd(follower,date.getTime(),String.valueOf(userId));
        //自己关注的这类实体+1
        tx.zadd(follwee,date.getTime(),String.valueOf(entityId));
        List<Object> ret= jedisAdapter.exec(tx,jedis);
        //查看返回值是否正常
        return ret.size() == 2 && (Long)ret.get(0)>0 && (Long) ret.get(1)>0;

    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean unfollow(int userId, int entityType, int entityId) {
//        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
//        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
//        Date date = new Date();
//        Jedis jedis = jedisAdapter.getJedis();
//        Transaction tx = jedisAdapter.multi(jedis);
//        // 实体的粉丝增加当前用户
//        tx.zrem(followerKey, String.valueOf(userId));
//        // 当前用户对这类实体关注-1
//        tx.zrem(followeeKey, String.valueOf(entityId));
//        List<Object> ret = jedisAdapter.exec(tx, jedis);
//        return ret.size() == 2 && (Long) ret.get(0) > 0 && (Long) ret.get(1) > 0;
        //获取follower followeekey
        String follower = RedisKeyUtil.getFollowerKey(entityType,entityId);
        String follwee = RedisKeyUtil.getFolloweeKey(userId,entityType);

        //获取事务
        Jedis jedis = jedisAdapter.getJedis();
        Transaction tx =  jedisAdapter.multi(jedis);
        //实体的粉丝减少当前用户
        tx.zrem(follower,String.valueOf(userId));
        //对当前用户减少此类实体-1
        tx.zrem( follwee , String.valueOf(entityType));
        List<Object> ret= jedisAdapter.exec(tx,jedis);
        return ret.size() == 2 && (Long)ret.get(0)>0 && (Long) ret.get(1)>0;
    }

   /* public List<Integer> getFollowers(int entityType, int entityId, int count) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, 0, count));
    }*/

    public List<Integer> getFollowers(int entityType,int entityId,int count){
        String followKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followKey,0,count));
    }

    public List<Integer> getFollowers(int entityType, int entityId, int offset, int count) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return getIdsFromSet(jedisAdapter.zrevrange(followerKey, offset, offset+count));
    }

    public List<Integer> getFollowees(int userId, int entityType, int count) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, 0, count));
    }

    public List<Integer> getFollowees(int userId, int entityType, int offset, int count) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return getIdsFromSet(jedisAdapter.zrevrange(followeeKey, offset, offset+count));
    }

    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zcard(followerKey);
    }

    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return jedisAdapter.zcard(followeeKey);
    }

    private List<Integer> getIdsFromSet(Set<String> idset) {
       List<Integer> ids = new ArrayList<Integer>();
        for(String str : idset){
            ids.add(Integer.parseInt(str));
        }
        return ids;
    }

    /**
     *  判断用户是否关注了某个实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean isFollower(int userId, int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return jedisAdapter.zscore(followerKey, String.valueOf(userId)) != null;
    }
}
