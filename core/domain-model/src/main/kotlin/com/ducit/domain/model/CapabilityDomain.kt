package com.ducit.domain.model

/**
 * The canonical 18-capability ontology (Verbal Reference Implementation
 * v1.0, section 11.1; Inner Workings & Data Flow Architecture v1.1,
 * section 5.1). Capabilities describe what Ducit can do; connectors
 * describe how a specific environment does it. This enum is the single
 * source of truth for the ontology's membership and order — nothing else
 * in the codebase should hardcode a capability list.
 */
enum class CapabilityDomain(val id: String, val humanIntention: String) {
    CONNECT("connect", "Reach someone"),
    CAPTURE("capture", "Save this"),
    SEE("see", "Identify / translate"),
    HEAR("hear", "Record / transcribe"),
    GO("go", "Navigate to a destination"),
    KNOW("know", "Understand / research"),
    ACT("act", "Execute the next step"),
    CREATE("create", "Make / write / design"),
    PAY("pay", "Pay / redeem / show a pass"),
    PLAY("play", "Enjoy / discover entertainment"),
    PROTECT("protect", "Review / revoke / secure"),
    CONTROL("control", "Manage device / home / settings"),
    REFLECT("reflect", "Review progress / journal"),
    DISCOVER("discover", "Find an opportunity or resource"),
    COORDINATE("coordinate", "Align people / schedule / plan"),
    AUTHENTICATE("authenticate", "Prove identity / access"),
    REMEMBER("remember", "Save / retrieve / return to"),
    SENSE("sense", "Detect context / read environment"),
}
