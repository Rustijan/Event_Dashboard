package com.epam.eventhub.filter;


import com.epam.eventhub.config.SessionAttributes;
import com.epam.eventhub.session.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());
        if(!isProtectedPath(path)){
            filterChain.doFilter(request,response);
            return;
        }
        HttpSession session = request.getSession(false);
        if(session == null){
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        AuthenticatedUser currentUser= (AuthenticatedUser) session.getAttribute(
                SessionAttributes.CURRENT_USER
        );
        if(currentUser == null){
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        filterChain.doFilter(request,response);
    }
    private boolean isProtectedPath(String path){
        return path.startsWith("/organizer/")
                || path.equals("/profile/registrations")
                || path.matches("/sessions/\\d+/register")
                || path.matches("/registrations/\\d+/cancel");
    }
}
