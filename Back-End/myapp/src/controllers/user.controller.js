import sequelize from '../config/connect.js';
import initModels from "../models/init-models.js";
import bcrypt from 'bcrypt';
import { Op } from 'sequelize';

const model = initModels(sequelize);

const getProfile = async (req, res) => {
   try {
      const currentUserId = req.user.id;

      const user = await model.users.findOne({
         where: { id: currentUserId },
         attributes: ['id', 'username', 'email', 'avatar_url']
      });

      if (!user) {
         return res.status(404).json({ message: "User not found" });
      }

      res.status(200).json({
         id: user.id,
         username: user.username,
         email: user.email,
         avatar_url: user.avatar_url,
         created_at: user.createdAt
      });
   } catch (error) {
      console.error(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const getUser = async (req, res) => {
   try {
      const currentUserId = req.user.id;

      const user = await model.users.findOne({ where: { id: currentUserId } });

      if (!user) {
         return res.status(404).json({ message: "User not found" });
      }

      const friendships = await model.friends.findAll({
         where: {
            status: 'accepted',
            [Op.or]: [
               { user_id: user.id },
               { friend_id: user.id }
            ]
         }
      });

      const friendIds = friendships.map(f =>
         f.user_id === user.id ? f.friend_id : f.user_id
      );

      const friends = await model.users.findAll({
         where: { id: friendIds },
         attributes: ['username', 'avatar_url']
      });

      const friendUsernames = friends.map(f => f.username);

      res.status(200).json({
         username: user.username,
         avatar_url: user.avatar_url,
         friends: friends
      });

   } catch (error) {
      console.log(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const deleteUser = async (req, res) => {
   try {
      const currentUserId = req.user.id;

      const user = await model.users.findOne({ where: { id: currentUserId } });
      if (!user) {
         return res.status(404).json({ message: "User not found" });
      }

      await user.destroy();
      res.status(200).json({ message: "User deleted successfully" });
   } catch (error) {
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const updateUser = async (req, res) => {
   try {
      const currentUserId = req.user.id;
      const { username, email, password } = req.body;
      const file = req.file;

      const user = await model.users.findOne({ where: { id: currentUserId } });
      if (!user) {
         return res.status(404).json({ message: "User not found" });
      }

      if (username) {
         user.username = username;
      }

      if (email) {
         user.email = email;
      }

      if (password) {
         user.password_hash = bcrypt.hashSync(password, 10);
      }

      if (file) {
         const host = process.env.HOST_URL || 'http://localhost:3000';
         const image_url = `${host}/uploads/${file.filename}`;
         user.avatar_url = image_url;
      }

      await user.save();
      res.status(200).json({ message: "User updated successfully" });
   } catch (error) {
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const addFriend = async (req, res) => {
   try {
      const { friendUsername } = req.body;
      const currentUser = req.user;

      const friend = await model.users.findOne({ where: { username: friendUsername } });
      if (!friend) {
         return res.status(404).json({ message: "Friend not found" });
      }

      // Kiểm tra nếu A đã gửi lời mời cho B (A là currentUser)
      let existingFriend = await model.friends.findOne({
         where: {
            user_id: currentUser.id,
            friend_id: friend.id
         }
      });

      if (existingFriend) {
         if (existingFriend.status === 'pending') {
            return res.status(400).json({ message: "Friend request already sent" });
         }
         if (existingFriend.status === 'rejected') {
            existingFriend.status = 'pending';
            await existingFriend.save();

            await model.notifications.create({
               user_id: friend.id,
               sender_id: currentUser.id,
               type: 'friend_request'
            });

            return res.status(200).json({ message: "Friend request resent" });
         }
         if (existingFriend.status === 'accepted') {
            return res.status(400).json({ message: "You are already friends" });
         }
      }

      // Kiểm tra nếu B đã từng gửi lời mời cho A (friend là user_id, currentUser là friend_id)
      const reverseFriend = await model.friends.findOne({
         where: {
            user_id: friend.id,
            friend_id: currentUser.id
         }
      });

      if (reverseFriend) {
         if (reverseFriend.status === 'pending') {
            return res.status(400).json({ message: "You already have a request from this user" });
         }
         if (reverseFriend.status === 'rejected') {
            // Xoá bản ghi cũ để gửi lại từ phía currentUser
            await reverseFriend.destroy();
         }
         if (reverseFriend.status === 'accepted') {
            return res.status(400).json({ message: "You are already friends" });
         }
      }

      // Tạo mới lời mời kết bạn
      await model.friends.create({
         user_id: currentUser.id,
         friend_id: friend.id,
         status: 'pending'
      });

      await model.notifications.create({
         user_id: friend.id,
         sender_id: currentUser.id,
         type: 'friend_request'
      });

      return res.status(200).json({ message: "Friend request sent successfully" });
   } catch (error) {
      console.error(error);
      return res.status(500).json({ message: "Internal Server Error" });
   }
};



const unFriend = async (req, res) => {
   try {
      const { friendUsername } = req.body;
      const currentUser = req.user;

      const friend = await model.users.findOne({ where: { username: friendUsername } });
      if (!friend) {
         return res.status(404).json({ message: "Friend not found" });
      }

      const friendship = await model.friends.findOne({
         where: {
            [Op.or]: [
               { user_id: currentUser.id, friend_id: friend.id },
               { user_id: friend.id, friend_id: currentUser.id }
            ]
         }
      });

      if (!friendship) {
         return res.status(404).json({ message: "Friendship not found" });
      }

      await friendship.destroy();

      res.status(200).json({ message: "Friend removed successfully" });
   } catch (error) {
      console.error(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const acptFriend = async (req, res) => {
   try {
      const { friendUsername } = req.body;
      const currentUser = req.user;

      const friend = await model.users.findOne({ where: { username: friendUsername } });
      if (!friend) {
         return res.status(404).json({ message: "Friend not found" });
      }

      const friendship = await model.friends.findOne({
         where: {
            user_id: friend.id,
            friend_id: currentUser.id,
            status: 'pending'
         }
      });

      if (!friendship) {
         return res.status(404).json({ message: "Friend request not found" });
      }

      friendship.status = 'accepted';
      await friendship.save();

      await model.notifications.create({
         user_id: friend.id,
         sender_id: currentUser.id,
         type: 'friend_accepted'
      });

      res.status(200).json({ message: "Friend request accepted successfully" });
   } catch (error) {
      console.error("Lỗi acceptFriend:", error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const rejectFriend = async (req, res) => {
   try {
      const { friendUsername } = req.body;
      const currentUser = req.user;

      const friend = await model.users.findOne({ where: { username: friendUsername } });
      if (!friend) {
         return res.status(404).json({ message: "Friend not found" });
      }

      const friendship = await model.friends.findOne({
         where: {
            user_id: friend.id,         // người gửi lời mời
            friend_id: currentUser.id,  // người nhận (mình)
            status: 'pending'
         }
      });

      if (!friendship) {
         return res.status(404).json({ message: "Friend request not found" });
      }

      friendship.status = 'rejected';
      await friendship.save();

      res.status(200).json({ message: "Friend request rejected successfully" });
   } catch (error) {
      res.status(500).json({ message: "Internal Server Error" });
   }
};


const searchUsers = async (req, res) => {
   try {
      const { name } = req.query;
      const currentUser = req.user;

      if (!currentUser.id) {
         return res.status(401).json({ message: "Unauthorized" });
      }

      if (!name) {
         return res.status(400).json({ message: "Name of person is required" });
      }

      // Tìm tất cả user đã là bạn bè accepted
      const acceptedFriends = await model.friends.findAll({
         where: {
            status: 'accepted',
            [Op.or]: [
               { user_id: currentUser.id },
               { friend_id: currentUser.id }
            ]
         }
      });

      const friendIds = acceptedFriends.map(f =>
         f.user_id === currentUser.id ? f.friend_id : f.user_id
      );

      const excludeIds = [currentUser.id, ...friendIds];

      const users = await model.users.findAll({
         where: {
            username: {
               [Op.like]: `%${name}%`
            },
            id: {
               [Op.notIn]: excludeIds
            }
         },
         attributes: ['username', 'avatar_url']
      });

      res.status(200).json(users);
   } catch (error) {
      console.error(error);
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const getAllRequests = async (req, res) => {
   try {
      const currentUser = req.user;

      const requests = await model.friends.findAll({
         where: {
            friend_id: currentUser.id,
            user_id: currentUser.id,
            status: 'pending'
         }
      });

      // Tìm ID của người còn lại (không phải currentUser)
      const otherUserIds = requests.map(r =>
         r.user_id === currentUser.id ? r.friend_id : r.user_id
      );

      const users = await model.users.findAll({
         where: {
            id: {
               [Op.in]: otherUserIds
            }
         },
         attributes: ['id', 'username']
      });

      // Ghép username với status
      const result = requests.map(r => {
         const otherUserId = r.user_id === currentUser.id ? r.friend_id : r.user_id;
         const user = users.find(u => u.id === otherUserId);

         return {
            username: user?.username || null,
            status: r.status
         };
      });

      res.json(result);
   } catch (error) {
      res.status(500).json({ message: "Internal Server Error" });
   }
};

const getSentRequests = async (req, res) => {
   try {
      const currentUser = req.user;

      // Lấy tất cả các friend requests liên quan đến currentUser có status pending hoặc rejected
      const requests = await model.friends.findAll({
         where: {
            friend_id: currentUser.id,
            status: 'pending'
         }
      });

      // Tìm ID của người còn lại (không phải currentUser)
      const otherUserIds = requests.map(r =>
         r.user_id === currentUser.id ? r.friend_id : r.user_id
      );

      const users = await model.users.findAll({
         where: {
            id: {
               [Op.in]: otherUserIds
            }
         },
         attributes: ['id', 'username']
      });

      // Ghép username với status
      const result = requests.map(r => {
         const otherUserId = r.user_id === currentUser.id ? r.friend_id : r.user_id;
         const user = users.find(u => u.id === otherUserId);

         return {
            username: user?.username || null,
            status: r.status
         };
      });

      res.json(result);
   } catch (error) {
      res.status(500).json({ message: "Internal Server Error" });
   }
}


export {
   getUser,
   deleteUser,
   addFriend,
   updateUser,
   unFriend,
   rejectFriend,
   acptFriend,
   searchUsers,
   getProfile,
   getAllRequests,
   getSentRequests
}