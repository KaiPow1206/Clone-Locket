import express from 'express';
import { extendToken, loginAccount, logoutAccount, signUpAccount } from '../controllers/auth.controller.js';
const authRouter = express.Router();

authRouter.post(`/login`,loginAccount);
authRouter.post(`/sign-up`,signUpAccount);
authRouter.post(`/refresh-token`, extendToken);
authRouter.post(`/logout`, logoutAccount);
export default authRouter;