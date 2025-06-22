import jwt from 'jsonwebtoken';
import dotenv from 'dotenv';

//đọc file env
dotenv.config();

//create token 
export const createToken = (data) => {
  return jwt.sign({payload:data},process.env.ACCESS_TOKEN_KEY,{
   algorithm: "HS256",
   expiresIn: "5m"
  })
}

export const verifyToken = (token) => {
  try {
    return jwt.verify(token, process.env.ACCESS_TOKEN_KEY);
  } catch (error) {
    return null;
  }
};

// verify refresh token
export const verifyRefToken = (token) => {
  try {
    return jwt.verify(token, process.env.REFESH_SECRET);
  } catch (error) {
    return null;
  }
}

// refresh token
export const createRefToken = (data) => {
   return jwt.sign({payload:data},process.env.REFESH_SECRET,{
      algorithm: "HS256",
      expiresIn: "7d"
     })
}


//create middleware token
export const middlewareToken = (req, res, next) => {
  const {token} = req.headers;
  if (!token) {
    return res.status(401).json({ message: "Token missing" });
  }
  let decoded= verifyToken(token);
  if (decoded) {
    req.user = { id: decoded.payload?.id };
    next();
  }
  else {
    return res.status(401).json({ message: "Unauthorized token" });
  }
};