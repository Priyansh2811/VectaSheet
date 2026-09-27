package com.vectasheet.config;

import com.vectasheet.entity.*;
import com.vectasheet.repository.UserRepository;
import com.vectasheet.repository.WorkspaceMemberRepository;
import com.vectasheet.repository.WorkspaceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository memberRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmailIgnoreCase("demo@vectasheet.com")) {
            return; // already seeded
        }

        User demoUser = new User();
        demoUser.setName("Priyanshu Rao");
        demoUser.setEmail("demo@vectasheet.com");
        demoUser.setPasswordHash(passwordEncoder.encode("Demo1234!"));
        demoUser.setEmailVerified(true);
        demoUser = userRepository.save(demoUser);

        Workspace workspace = new Workspace();
        workspace.setName("Demo Workspace");
        workspace.setDescription("A sample workspace with a product launch project.");
        workspace.setOwnerId(demoUser.getId());
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspaceId(workspace.getId());
        member.setUserId(demoUser.getId());
        member.setRole(WorkspaceRole.OWNER);
        memberRepository.save(member);

        System.out.println("============================================");
        System.out.println(" VectaSheet demo data seeded");
        System.out.println(" Login: demo@vectasheet.com / Demo1234!");
        System.out.println("============================================");
    }
}
