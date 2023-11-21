package position;

import ChessUtilities.Util;

import java.security.SecureRandom;

public class Zobrist {

    private static final long whiteToMoveRandom = 0x65885804c2f2fc0cL;
    private static final long[] enPassantFileRandom = {
            0xef704a1699e4b7b4L,
            0x29d5e8c7d7e1efdaL,
            0x78c4e8cb7a44a6fL,
            0xdc4d5095d4ae7598L,
            0x499a23ebf1723cd8L,
            0xf391c5d6f6901e2bL,
            0x4760df02c4c4e2ceL,
            0xe7411f251711255eL,
    };
    private static final long[] castlingRightsRandom = {
            0xf7e68e2467b39446L,
            0x2a32ebe153011a47L,
            0x9372e4de047fb849L,
            0xf339513b6af36e98L,
    };
    private static final long[][] pieceOnSquareRandom = {
            {},
            {//white pawn
                    0x32b65bd2ac900d23L, 0xc338213df3f668eeL, 0x44c6adb9f876a09aL, 0xf478fc12e2006947L,
                    0x72ee13566cd3c484L, 0x8f1222a72a630ac7L, 0x3eca6b64e1d63924L, 0x4390f52a5f6653aL,
                    0x97a04feb6fe9fb34L, 0xe8b1cf4b94c11344L, 0xd590f1cdbf066c30L, 0x980708c4a7a5710dL,
                    0xee6e9dd76025475bL, 0xf44d697102d8af66L, 0xb843e5689de08a21L, 0xff0fd0439b9b39cdL,
                    0xba453f21e89cc132L, 0xc1ffcedf031f9c4cL, 0xbf8b8beb369728a6L, 0x14f337e9927a02a4L,
                    0x4a308287da5e7166L, 0x14b139078064469L, 0x2cf46285c9155fbdL, 0xfa0a5ed017c2ff55L,
                    0x96e8e76d753da93cL, 0xdbcbc27a7432c35fL, 0xa95bfb99194fb056L, 0x98180f19e9a41461L,
                    0xc87365f75c1af75L, 0xffcad24157540741L, 0xd442dd65020d1c59L, 0x44cbdcb705734262L,
                    0xf2f0df64bf608866L, 0xf6210672d6140f0dL, 0x7f094a0e8c0d98ebL, 0xaca9898fe38933a8L,
                    0xe95167f4ed8cb26cL, 0x998e214589d30a3cL, 0xb170eafa74687deL, 0x570c21a5f2feb6b4L,
                    0xe35672163cb6f830L, 0xe06ba3c5cb0d0f27L, 0x3dac0a26b2d4d4d3L, 0x1670b5d2070cbfceL,
                    0x30a5595229e0ee47L, 0x199a5f7cf88523ffL, 0xd4360cdffe4e0a79L, 0x19adb31c014564dfL,
                    0x8b9be5c826557cf6L, 0x1a4d3bcc816b4e90L, 0x9d61f666bc8d01faL, 0xb850a7dda2478dc4L,
                    0xf6015e2fb5ae5fdbL, 0xd66e8bd270f347b8L, 0xe2685df7165c50a4L, 0xa11433970090930eL,
                    0x1469f9f15b988ac4L, 0x3fc75df668510487L, 0xee1ae6542ea79b04L, 0xfc637724837a2c5L,
                    0x75dafd5b5c0a840bL, 0xb51d433a4a56fa9fL, 0xb94512d94573470dL, 0xb6b240d1c524cf61L,
            },
            {//white knight
                    0x6251ebb679891313L, 0x838a9e2755641990L, 0x634b4161184f4e0bL, 0x65965b47ad15af2bL,
                    0xad863588de8993e4L, 0x2259a474fa3b67c9L, 0x88dce01c4001c1deL, 0x24878838ef03b7f3L,
                    0xd1fbfcbcb2535084L, 0x12668c3c9335aa51L, 0x36a7f53b4505157aL, 0x54d4364a370a1b4cL,
                    0x7c49210f497ce0abL, 0x9f2ec5c7bae34933L, 0x1a5e503c7e0470a0L, 0x4739a56e6c65534dL,
                    0x6204d69094a51a66L, 0xeceaa89012a3543eL, 0xe676aeca6a5a01d9L, 0x3c901306012a0b67L,
                    0xd156b3ff04e699d7L, 0xc493061c0cc3e83aL, 0x38d98adc88ee30eL, 0x6f0a840a41d42c8cL,
                    0x79ecf082356391c5L, 0x77d5fb65a7586d3bL, 0xb7900e848a6d5c6aL, 0x970ef492748661d1L,
                    0x91b8ca79004ddd03L, 0x124dc8b5003062d2L, 0xbc9d6bbc7ef7ae8bL, 0xa606dfc82d3c6fe2L,
                    0x16f3dcabb7d83a53L, 0x90737e2c075fa2f6L, 0x9983caeeb26457L, 0xfd1c19dc3ade7713L,
                    0xa03121dc108ae639L, 0x31a9f1cfc798efbdL, 0xdd955290379398c9L, 0x4851cdc1fedaf5e0L,
                    0xe17f0744c6ebf134L, 0x496cd9979576f861L, 0xf6bea43770635d3cL, 0x57f8446c283c80efL,
                    0x550cecf11616c5d3L, 0x48a68824afaf9485L, 0x47166edaf2eb967cL, 0xedd13ed48ca6a28dL,
                    0x8c4934229a8c364fL, 0x4ab9e0b6ae88bcbaL, 0x53a8f6aa60859905L, 0x4a56e5739e21d96eL,
                    0x88de47ef2ecb140aL, 0x9635e21dc6190aebL, 0x390ef1d5b4576164L, 0x1f7cb77675159a47L,
                    0xfa0f37f8e9800286L, 0x90bdee2454a491f3L, 0x709807a6d8bb6d0eL, 0x1471ca4d9064ef97L,
                    0xe993a53c0f42e2b2L, 0xb03ad67ce5eff6beL, 0x12b49a334d83486fL, 0x86b703943f00eb5L,
            },
            {//white bishop
                    0xf9b515fce6fd0bbeL, 0x445978e002ae4342L, 0xc0a66f4c771571b2L, 0x7e27e95bfa3e7765L,
                    0x8dd05e05898aa3aaL, 0x89cbadf2768b9f9cL, 0xb19626ec85ba618bL, 0xbc39959762def5e1L,
                    0xd8846e6b9d93cfb5L, 0xfae57132db6ca26dL, 0x58111a896ebfbd49L, 0xc0081d4bd3fb5bf3L,
                    0xe52ff97f9759d0cfL, 0x9d35c125fa1e4baaL, 0x5e1afb324b19c2ebL, 0xc2f2f3b5ab3364cdL,
                    0x986db9bf42e42a82L, 0xb8b4f2fa5776ba8eL, 0x27252b068bcf263aL, 0x8627b80ebad5a56bL,
                    0xa51356d0b5548603L, 0xfbfeae0b05032aa1L, 0x46d321cd83a959e1L, 0x69e45e2ac8c041b6L,
                    0xd1998347f0adc75dL, 0x9efe6f2768ee680L, 0x342d72234f119e50L, 0x9bb7dad267666d58L,
                    0xbfe6fbd9040b4adbL, 0xcac4f1b900656a9eL, 0xb9db90395a6ff422L, 0x9304ec0b34b47942L,
                    0x73bd8eabb9dac5dcL, 0xff86328707ee2331L, 0x48cfb92a864b4df6L, 0xc344fa2858869816L,
                    0xbfe33dbcf1f1c736L, 0x132babca0f3406b9L, 0xec597d734d4e5df6L, 0x24011e78bcc3d395L,
                    0xaab2c0cee39e79a9L, 0x48181e2f02985d33L, 0x719e205c844c5c6bL, 0xe1406a7fbc99b919L,
                    0xa13e3282a53a942L, 0x64e2696884610174L, 0x5073e0726f1da6afL, 0x1047c50b794bf743L,
                    0x1512507853818f24L, 0xffc7085aef07dfbcL, 0xd9e1b77298bd831dL, 0xc098e3fc8c4c979aL,
                    0x21b5a9d5657a75feL, 0x4a777d9e5f40252bL, 0xee18a01012d91a4dL, 0x17bdde1afaa09de2L,
                    0x83cc386ab0091c48L, 0x73f7b1f28e07f31aL, 0xc2c97797dfd8e296L, 0x92d1314c2ad9086eL,
                    0x1195d20975dc9fb6L, 0xca1276846c3823L, 0x75949a7309d81efeL, 0x1049def31af894b0L,
            },
            {//white rook
                    0x6387b10ee3324d90L, 0x84f5b6b676540288L, 0x645f4ebff5104142L, 0xa16b400e46fd210L,
                    0xe65cf554e0427a39L, 0x4300a3d8e1dd0fbdL, 0xba061ed5ca15a7bbL, 0xc518619456d09e26L,
                    0xeb70e4c33a5edb31L, 0x66a37050fb8052f3L, 0xeabf487daa121c02L, 0x6beadae6c4674096L,
                    0xc405b8ef0d9d46c5L, 0x536662ef3bfe29baL, 0x5ce3bf921b3ca100L, 0x8c57b58665a46645L,
                    0x33401d1955c71d54L, 0xe5385e54b754165L, 0x6454ac3abc0a6decL, 0xb9c9cd3e2896eda7L,
                    0xea6871ffc2f6d123L, 0x7faa506b8ea3d422L, 0xb15f78b8ef757955L, 0x707d33647fe4ef6dL,
                    0xef0e4e5984e0c6e0L, 0xcaa0016e7d4538f3L, 0xd15951d08bd1d818L, 0x37c356080a5721e4L,
                    0x402b7ac73f34836dL, 0x5bfcd5b55181f5d0L, 0xc445bab27baed9adL, 0x6eccf6f8892dfedcL,
                    0x2567c6cb6a2607a8L, 0x71ed32f8abb4ef93L, 0x3a7f35b3b31510f0L, 0x1a498212b6cfda5fL,
                    0x22a2c0d95da62fb8L, 0x8ac8c8586612bfceL, 0x532f1af98a45ae0L, 0x70e9cea48617ad01L,
                    0x617026590df5873aL, 0x1dd7adb5e85e3b3aL, 0x15c0b6ebfcb38c50L, 0xeb34e11d673c93f2L,
                    0xd3a3f1f5a98f3e00L, 0x3014d00deb7c1c82L, 0x6ecdcb572e529451L, 0x51b80d5aac2e0895L,
                    0x9f2e4f54d291b6cdL, 0x128785c29a52b42fL, 0xd3748101f8c2b010L, 0x1eb88aeadf477012L,
                    0x8c0c874eb619fadcL, 0x1ad97e512d0be84L, 0x575658e44af6ac8dL, 0xa264f3f8c37d5fafL,
                    0x43ff7a045710474dL, 0x9b7acf70b8418916L, 0x40d2bcac07e84e19L, 0xdb954413f179d8adL,
                    0x694ae6344b20058L, 0x723e0451c7eeeb7dL, 0x30ac7a387edbece6L, 0x3aa70c83fd9c1240L,
            },
            {//white queen
                    0x15f16bc1dc266376L, 0x39c45834f1009457L, 0xff6b31e3437bb163L, 0x9f8c4e9987b013bfL,
                    0x6ba831981ba685e2L, 0xdf0cafaf9738de3cL, 0xdc0abf7803adeb49L, 0x9d02b46e1a68258L,
                    0x7b4d5ce9bee5ec14L, 0x74f98ee5c4bc614eL, 0xd41f6765d34fc01eL, 0x16b49c25fb091910L,
                    0xedd61ba8bab893b0L, 0x7c0bc739d20e6ff7L, 0x5801abb6b99aa9c6L, 0x225896f0007af2e4L,
                    0xff06e2d9b6802bdcL, 0x535d6cfaee203c6L, 0x5436630e02fc30f4L, 0xe00074e4aa9a6b1aL,
                    0x2c561a37241cfdeeL, 0xd808f57987f68559L, 0x3587f71b356afbc9L, 0x5fb9bb6b778fc13L,
                    0xc0c0614532f73499L, 0xc430a9886eacc62cL, 0xc55fb000c02667b1L, 0x95bb5cc4ccf7d7a6L,
                    0x4211034f98e4fa91L, 0xa88473527e3f183dL, 0x909eb4122dbbdda7L, 0x613c7d7a8f35c9f3L,
                    0x336246d0eebd5fe4L, 0x32b83168d83895b4L, 0x4ce091431f1e5307L, 0x38249e9ed461c7c9L,
                    0x8d7b869e652de5c9L, 0x5f2c4a7e7b93af0bL, 0xe8a4a5cd7782afccL, 0x3f0d0cd1ffa7ece8L,
                    0x87146a1e6d4374ecL, 0x80a00771b7807028L, 0x3d8fca5bd828bb2dL, 0x8111bcaae42f2edfL,
                    0x63afc6c0e5a8fcd4L, 0x81fed2ee8a20eedbL, 0x515b8ed7371961fdL, 0xbe44a45ad2816a3dL,
                    0x6778ddd4cb73fb0L, 0xcc7ea7e035105347L, 0x76099f5ab628edaL, 0xc3b94b92533770f4L,
                    0x92bfc46ea2f88b87L, 0xe82188f485141d56L, 0xa5c1eb7317878eb5L, 0xb464c6b82319fbc4L,
                    0xc48019109dd0141eL, 0xf24c70e51f678119L, 0x987134dc5cd4315fL, 0xd3624bea56aa63adL,
                    0xddc633490c3946dcL, 0xb9948ac6b7840386L, 0x4262aadcd84a25c6L, 0x9d3406541e1755bL,
            },
            {//white King
                    0x4e8fbb906baab1d3L, 0xbf567f0ff65adfa9L, 0xccb063f2bc65d23bL, 0xc44608847126b7eaL,
                    0x9ce85ff31a04e369L, 0x259a4f0bac75f109L, 0xada936b05339f6f0L, 0xc748e4756a80c96dL,
                    0x56883bfdb7675a7bL, 0xcc535acf7d8ab002L, 0x8709c1428abfc323L, 0xd119db21979d2741L,
                    0xc0a4c62f70da101aL, 0xd3365003f0773fe8L, 0xfdc573478012365eL, 0xce50711c81355f27L,
                    0x51e74ecf6dee2d74L, 0xe868606fdd8c9795L, 0x2452d132e849918L, 0xc2dfa8eaf8446b67L,
                    0x7b97301bec17162cL, 0xa948bd64ad9c5c9eL, 0xe85b96a659640e37L, 0x5a3c8c6d932fdd9fL,
                    0xf899fad7a55a4debL, 0x3a8f093cdf385550L, 0x8cd58838ef6b2009L, 0x4a51f35cb90a5a9aL,
                    0x5fc494c046387f66L, 0x95eca01d3dde3f4bL, 0xe47a0f69bb0b6699L, 0x7c8a1920630be210L,
                    0x631ec929e07594d3L, 0x35458e4002227becL, 0x539aa48896952fd8L, 0x57339ca1460cf864L,
                    0xb8589fdb3e1bef8dL, 0x2cc8909427025c22L, 0xa3923dd83fc8b21dL, 0x8466c9504803db95L,
                    0x5d4daec041bbeba4L, 0x87fec33573ed5ef1L, 0x23cb90f60be6ff78L, 0x9fa3d342ac2ae2b2L,
                    0x991420b02180e2daL, 0x181c035ce1ed509cL, 0xa8cc652acf225ea1L, 0xf316003a3c0f2f74L,
                    0x249c246680f71f17L, 0x755ffac4c0be084cL, 0x295160c02e8d8e8dL, 0x186c47ab0bf7411aL,
                    0x9cf16d8eb1adca50L, 0x88702e34a58415c7L, 0x1b3e3b5bd2ae4211L, 0x5523498da3b4a354L,
                    0x3812edf971fd770bL, 0x747d98248add4919L, 0xd81f130136fff649L, 0x54ea3d8454217a2bL,
                    0xc2434f77279cbd5eL, 0x4f94a2887c4a87b0L, 0x70b21c5d806e6a7eL, 0x8e7c7f5aaceb334cL,
            },
            {},
            {},
            {//black pawn
                    0xa9bd8c7841f155a1L, 0x2875cc4f1e12f218L, 0x7447a843f8238105L, 0x5087ace5f7f3e6cdL,
                    0xd98150c8c5a6b374L, 0x5a0bdf226da10a59L, 0x28333ff018abb343L, 0x374a5edcc8652002L,
                    0xa50277fb2e7db9bdL, 0xf9ee5992100e83b4L, 0x1b7575c6ee7a38aeL, 0xbf49c27920c0a665L,
                    0x474c9fce11bc0ca3L, 0xac59019fe5b22e7eL, 0xd4f211665e5c6331L, 0x10535084e2cfcd45L,
                    0x915ee61aac316eecL, 0xb113c233b7d453bbL, 0x7f02cf08c06e89c6L, 0x1be910dcb9727674L,
                    0x77960a3240cab1b6L, 0x623190dd8f3e7120L, 0xfc1c6cacfd3b152fL, 0x806ed2f400a17d96L,
                    0xd57ca8df0b9863ffL, 0x5ac4a946af2914edL, 0x9a29b36052e9ed98L, 0x7569e8e6584a65b8L,
                    0x43f5a5bcbc149b1bL, 0x7f9ee6976a73ff61L, 0x791574bd12836b2eL, 0xb096d51ea2cf3d92L,
                    0xc50f9700b1bb1aa2L, 0xc08e8c02bf062663L, 0xa4f66ab59f88c5e0L, 0xd847e065bf192e34L,
                    0x7b0a1363bfecc123L, 0xf28a41448df45010L, 0x1a44c3f0ba25a52aL, 0x9c517f6916b14c94L,
                    0x6f9142fb77756064L, 0xb30d6e0cff0fa2a0L, 0xca8c748f94b49282L, 0xe9a2408b2c913b3eL,
                    0x3bd89ba76f5f782dL, 0x26f4f0bbe6e4969dL, 0xc7ad4e429f24fa05L, 0xc8bb9369e42597edL,
                    0x2401515f0e48086bL, 0xdc124fc4fbbb9480L, 0x97d0e297d86c836bL, 0x36b9c052240c575fL,
                    0x4a86b15e38b43e02L, 0x700c8454d56611edL, 0x6457f02b6c317bcbL, 0xbffb9724ce4204c0L,
                    0x24b8ddf793117003L, 0xfbfccf404176b08dL, 0x7c7f599a49cdc2a7L, 0xfe2f4ebd047d3acfL,
                    0x23d0dc43b812488L, 0xa714ee3d2950bf66L, 0x994667b44a75481L, 0xee473b88dde0639L,
            },
            {//black knight
                    0xb7a7398b2c95eb9L, 0x243cde0b1bb11b29L, 0xc193bf1f1ac31923L, 0x78e2457fbd01f514L,
                    0xea86a94618efdb63L, 0xd5b967a1f16571ddL, 0x8882e7aeadfae57fL, 0x4e52a6ba60b4f18bL,
                    0x171d422019a5e160L, 0x1bcd3c91c05d74abL, 0xde3ea3342e38d1faL, 0x6d162621b0cff729L,
                    0xc27885815de083dL, 0x978be06c5411a4eeL, 0xa7aaa153cf3a9268L, 0x8f5e2b608dcf42eeL,
                    0x164316af113e336dL, 0xc1097c156a6041dfL, 0xb6f93097343bca4eL, 0xc2738956e398f260L,
                    0x2446e6846089098aL, 0xcb2728a3baa9756dL, 0xcd2c5d351a17303aL, 0x39086ef5745385caL,
                    0xfc4c7042f35e9890L, 0x7e704a8e3292e6bcL, 0x6af0979a148104b7L, 0xb2a05dfc20c371b5L,
                    0x5c33a68a2375d92cL, 0x77bb3a90a759c457L, 0xc27b2e6add45c44bL, 0x9b6e51c8b5a6f0ffL,
                    0x750e32e907ef710fL, 0x25d0ff2f7d830c32L, 0xe3ded269c37c2187L, 0x908357e35953b41eL,
                    0xe44be9b31212e2b4L, 0xe84e081ff3de4a0fL, 0x14599bb7227fba1dL, 0x6e3500661e44eaf0L,
                    0x602a44b7aa2f6809L, 0x5ddc32c5d521f44eL, 0x51305c3e29bc06a7L, 0xd4435ce2b111d6dL,
                    0xcdc3dc2694a13df8L, 0xda91ff4a3c42607bL, 0x17b74826258e53ddL, 0x39b0e25e76f8816bL,
                    0x1f65f37666b21fa8L, 0x550913bf153386e6L, 0x7ea1979f636be481L, 0xa8ff23ac86335d94L,
                    0x929c76e4e3b37125L, 0x2ad89d7af3d76f5fL, 0xd8a12c7c4cd1745dL, 0x2a81d4e0c6e1ff4aL,
                    0xe2bfe8e19dce5476L, 0x2870a0347741413cL, 0xa8bf81b509616a4cL, 0x9c0372e7829d57ecL,
                    0xae8f99f7177eae0fL, 0x1ad8546bd6d56f46L, 0x623d45c99dccd58eL, 0xd33a726a5917b400L,
            },
            {//black bishop
                    0x4dd4e9da64542b19L, 0x18b09b4e2bf72c80L, 0xe4d96d75be3b4a93L, 0x1e1a9a25753c3d71L,
                    0x579e5ec0777b1febL, 0xc9f2acc39ed6f8c6L, 0xc0110b7d422abbe5L, 0x60e657e1e2bd2fadL,
                    0xda4bbbdac1bbd536L, 0x6c4a24e41ae1f199L, 0xd908dfcd93d9d80L, 0x1e50247bd585a3f0L,
                    0x1bf1e26a8569fe7eL, 0xdc9dabde0172c213L, 0x1e14031d99763a72L, 0x342b943f9f212178L,
                    0xec6b0690802607b9L, 0x37df41b6d55cbe9bL, 0xbf04c77a06ce29e4L, 0x7a0da7c11b0a64eaL,
                    0x70d7583713785a6fL, 0x3b813a10c4bfbed3L, 0xa4e3c667e7cb3833L, 0x84bcdfe7cdf7c367L,
                    0xd6f0d5bccae00da2L, 0xc1080022b33dfc13L, 0x8743a8d4ff9bb78fL, 0xdc37a228daf98745L,
                    0x779d3dd8c59bd97aL, 0x9b71e92ceaa249c9L, 0x32af1cd4e376b365L, 0x6fcd60740e16537cL,
                    0xbc9c5e8cf1381de8L, 0xc73c8057d5ad8L, 0xd10ffb1e2679095cL, 0xa9b4753ae0ac1131L,
                    0x67ad220a673bea2dL, 0x5e02ed3793404edL, 0xa5f3a2bb3d808646L, 0xd99b5d59d78f929cL,
                    0xa34558fae925cdb2L, 0xbf5d4d9d521c56ffL, 0x98cbbc6942832feaL, 0xaa010e4ac41cd151L,
                    0xcb875ca78f63e41aL, 0xd4ec2de707327ecaL, 0x342ac3b13bb380fdL, 0x6c8612fac7c00fc2L,
                    0x7c9e277363277ed5L, 0x7397bd41d1e2ea65L, 0xbb4401f671f1f815L, 0x8cf88a6012808885L,
                    0xb38865fa4ed7ea92L, 0x32d7f9a51ad29a1dL, 0x936db015ad5d49cbL, 0x5960cb3c03328003L,
                    0x753fee5aafb82b57L, 0xb9acf42ac362ab6fL, 0x3083740597a02dL, 0xaf2e4970c1e78117L,
                    0x3b3fa5fd13856960L, 0x1b713224e74ccdfbL, 0x4441c97d6eedd70bL, 0x5d1f2d0da8bf5d95L,
            },
            {//black rook
                    0x2f62a9c3a46a3f0bL, 0x4fc1266c7ce420f6L, 0xe421678ddd72065fL, 0xee1754caf1e87c08L,
                    0xea0368c1e358b04bL, 0x28a72aad860584d1L, 0xbfb85caec7df3936L, 0x683b783f47d9cc1aL,
                    0x2f55123be66507eaL, 0xd2a1dfd0bb12d2dcL, 0x5960520fac7bd70cL, 0x583d45b43418c2d2L,
                    0x898501deb2076f04L, 0xb52e77088c54934cL, 0x7764713b7b20f75fL, 0x3fcfe76afc8b40ccL,
                    0x2dd86b5d01e44700L, 0xa54a86f997b5897eL, 0x45889586a79fc99eL, 0xb46a86fe6fecd39eL,
                    0xd025e39b72bde142L, 0x728716d424161211L, 0xda1fc8c80c92d195L, 0xe5c379e5a0ff89b1L,
                    0x77eee1b7c8b4a8d7L, 0x6a8fd6639543f188L, 0x363637c7e9ce82e7L, 0x3d216ee86727c84aL,
                    0x3a8373f4bc04bb2aL, 0x977ab1b599062ec2L, 0xd7221a895c652b52L, 0x3ec99729e5c936fdL,
                    0xebbac3aa0402b553L, 0x7512cccc97717ffdL, 0x81a7b196381767f8L, 0xf88af284b5d0203cL,
                    0xeab12f120286a55bL, 0x70717d2704316aaL, 0x3189eed393f93338L, 0xa53b5326dedfc8d0L,
                    0xbf30727e0f3e0841L, 0x71e4917f79439b47L, 0xa20cded182684ba0L, 0x1fb159ed5f5a60cbL,
                    0xff1a91a2ae3b520L, 0x52e1505a58d80a04L, 0x42ad3b672d769ccaL, 0x3e43bb3f6fbac721L,
                    0xa1950a8d1266753aL, 0x78a32009ce2ca62aL, 0xa237ad9851c896f9L, 0x68fb222280b8c53dL,
                    0x57afb8aed30b94b8L, 0x251a4820f343fbe8L, 0x4ec002258cf0e913L, 0x753c2023167da189L,
                    0x1b473df98a411003L, 0x273b5c70c2b25be7L, 0x721252a49cbfd39cL, 0x68851ffeac02433fL,
                    0xfb68b38d759efcabL, 0x4da274722a0bbaf2L, 0x27031eaae74b7dabL, 0xcfe1e91f046fc7c6L,
            },
            {//black queen
                    0xdb2b6f0a08a7e0a9L, 0x874753b842f2c9c8L, 0x3a3be4c6cf1e21aaL, 0x6f5fe2de2c8944dbL,
                    0x78d801d31e49b8L, 0x7da7366b55a2cbd9L, 0xeb680f7af5babfe3L, 0x8d1f8372eb08d60aL,
                    0xf854a0dddc34117L, 0x909a898cf3566b0L, 0xd38cef671e9b60d6L, 0xa078b6f4d9fdc28dL,
                    0x3ab866aa02d1602bL, 0x2252d24e703f4f3bL, 0x7d1ce677809ecd75L, 0x5232abeb06184e28L,
                    0xb38808b827532a3cL, 0x1a6ffbc6b7641390L, 0xf4aa742bbafe0201L, 0x6fcb099ca0d9378aL,
                    0xf449ca35ff9ee508L, 0x38d546e28560b3a8L, 0x863e7f61c1a87ebeL, 0xe758c9f308d16024L,
                    0x3bc3549ea9064d60L, 0xab5e44071113b5dfL, 0x87902c664512b8a6L, 0xfe78b7ccfa868a63L,
                    0x227f9022a6ff2fe2L, 0x98f630cbd79b61c2L, 0xdfb31e23b377868dL, 0x20adeea500776d1eL,
                    0x8783a499fca4f263L, 0x3b26acd6e28ac528L, 0xc8e7d4fa4d106233L, 0xbf2b47dc48ecb679L,
                    0xc75300b631beb6b1L, 0x880f6f5afe366944L, 0xc58175f7ccc266c7L, 0x19da3339dfc78dfL,
                    0xe99bd0221fa2db9dL, 0x4f6ba0c975fc3cd5L, 0x9cb5d910920b0ff6L, 0x4547d1b54e0e8729L,
                    0xe2041b435f90b7deL, 0xd9e333768c89cb4fL, 0x411bc1e036d4b008L, 0x5eeb410e015a3715L,
                    0x9c143ceaf783c26fL, 0x8facb94d4147506eL, 0x6bf482cb53331d31L, 0xf9f90b9df177b66L,
                    0x793f6bf5a2e02f97L, 0x4730fd4ec079c345L, 0xf690f4c12c28e75fL, 0xcb68509163785564L,
                    0xe45816b0986de159L, 0x695667c0b8b0d252L, 0xddb9e1986b25f26aL, 0x919617e5d13b1a14L,
                    0x4bac2b1716eb1d91L, 0x7004ac9707a3c2f1L, 0x13970098a6e202b3L, 0xc69309815817a549L,
            },
            {//black king
                    0x268c2858688ccb13L, 0x787194d901d3eba5L, 0x376faf8e822df69cL, 0x6f7ce82c91bba8c5L,
                    0x7b908de882f63ab1L, 0xf399424db40a88e3L, 0x8f7bf84eb936b129L, 0xb9798959513ff360L,
                    0xb5b5737dceebcb9aL, 0x2129da86147e531dL, 0xfda6bb38217f9297L, 0x99f10355faef853aL,
                    0xcb617df0dc3d1c8dL, 0x3c3e965972517d52L, 0xcdeb2019b6269ddbL, 0x4fbd6c5e09783b68L,
                    0xd985ed3afc76bf9dL, 0x8922d7e32487ac9cL, 0xdec89ba8bb7981b4L, 0xc68176d1f23f86acL,
                    0x27a2a90cfc1dbd9cL, 0x42b19e1d305e1153L, 0x52a33f325f2035f8L, 0x914899540249d5dcL,
                    0xa3990268a43643e2L, 0xa80dafe1c407207eL, 0xed0b252cec5275b6L, 0x1f924911e3b54643L,
                    0xe047a6fbb7d46L, 0xe7c4b72f269e2a42L, 0x5e2c173cc8e84513L, 0xa930048d52f51057L,
                    0x28caa9db474a3e9cL, 0x3868f4c52c4bd522L, 0x77b98fc1277271e0L, 0x22277b89a24cb0beL,
                    0xea94407a4dc2f90bL, 0xc69850cd6e8f03baL, 0xd1486c34fe648192L, 0xb26a0197933da860L,
                    0x5bf71a294e1a9abL, 0x4aca1fe4eaf18f6bL, 0x8edebfa91afc532aL, 0x5e6b95de38d8cbbeL,
                    0xc191bac6819133cbL, 0xa37600292befdc24L, 0xbacc7462ce630ba0L, 0xad7c576d830ece66L,
                    0x9c5067a2a77db0bL, 0x6803cb179157e478L, 0x92860207122e03e0L, 0xcf54f2847d84101dL,
                    0x75debe0604e62e5fL, 0x4d96a93035ceacd1L, 0x3d97cf6a6245db7fL, 0xf182f68615f8fd43L,
                    0x745a31f632f73462L, 0x7360d9919b4f9fa9L, 0xcec0a0e13822e27bL, 0x21de1c0ffc4c3dbcL,
                    0x18392f20a448b237L, 0x4cc0d6f39bf1613aL, 0xa59ae5d0c57c8977L, 0x272f755b2c4abec7L,
            }

    };



    public static void generateZobristKeys() {//used once, now have zobrist keys saved, so they are the same every time
        SecureRandom random = new SecureRandom();

        //whiteToMoveRandom = random.nextLong();//white

        for (int i=0;i<8;i++) {
            enPassantFileRandom[i] = random.nextLong();
        }

        for (int i=0;i<4;i++) {
            castlingRightsRandom[i] = random.nextLong();
        }

        for (int square = 0; square<64; square++) {
            for (int color = Type.White; color <= Type.Black; color += Type.Black) {
                for (int piece = Type.Pawn; piece<= Type.King; piece++) {
                    pieceOnSquareRandom[color | piece][square] = random.nextLong();
                }
            }
        }
    }

    public static long getZobristKeyFromPosition(boolean whiteToMove, long castlingRights, int enPassantTargetFiles, byte[] squareCentricPos) {
        long ret = 0;

        ret ^= whiteToMove ? Zobrist.whiteToMoveRandom : 0;

        if ((castlingRights & Type.whiteCanCS) !=0){
            ret ^= castlingRightsRandom[0];
        }
        if ((castlingRights & Type.whiteCanCL) !=0){
            ret ^= castlingRightsRandom[1];
        }
        if ((castlingRights & Type.blackCanCS) !=0){
            ret ^= castlingRightsRandom[2];
        }
        if ((castlingRights & Type.blackCanCL) !=0){
            ret ^= castlingRightsRandom[3];
        }

        if (enPassantTargetFiles != 0)ret ^= enPassantFileRandom[Long.numberOfTrailingZeros(enPassantTargetFiles)];

        for (int square=0;square<64;square++) {
            byte piece = squareCentricPos[square];
            if (piece!=0)ret ^= pieceOnSquareRandom[piece][square];
        }

        return ret;
    }

    public static long getKeyForMovingColor() {
        return whiteToMoveRandom;
    }

    public static long getKeyFromEPFile(int enPassantTargetFiles) {
        if (enPassantTargetFiles != 0)return enPassantFileRandom[Long.numberOfTrailingZeros(enPassantTargetFiles)];
        return 0;//no en passant target files, nothing to multiply by
    }

    public static long getKeyFromCastlingRights(long castlingRights) {//takes an input of a one bit for the toSquare of castling
        long ret = 0;
        if ((castlingRights & Type.whiteCanCS) !=0)ret ^= castlingRightsRandom[0];
        if ((castlingRights & Type.whiteCanCL) !=0)ret ^= castlingRightsRandom[1];
        if ((castlingRights & Type.blackCanCS) !=0)ret ^= castlingRightsRandom[2];
        if ((castlingRights & Type.blackCanCL) !=0)ret ^= castlingRightsRandom[3];
        return ret;
    }

    public static long getKeyFromPieceAndSquare(int piece, int square) {
        return pieceOnSquareRandom[piece][square];
    }


    public static void printAllZobristKeys() {
        System.out.println("White to move key: "+"0x"+Long.toHexString(whiteToMoveRandom)+"L");

        System.out.println("\nEn passant file keys: ");
        for (int i=0; i<8; i++) {
            System.out.println("0x"+Long.toHexString(enPassantFileRandom[i])+"L,");
        }

        System.out.println("\nCastlingRights keys: ");
        for (int i=0; i<4; i++) {
            System.out.println("0x"+Long.toHexString(castlingRightsRandom[i])+"L,");
        }

        System.out.println("\nPiece on Square Keys");
        for (int color=0; color <=8; color +=8) {
            for (byte piece = Type.Pawn; piece <= Type.King; piece++) {
                String pColor = color==0 ? "White " : "Black ";
                System.out.println(pColor + Util.getPieceStringFromShort(piece)+" Keys: ");
                for (int square = 0; square < 64; square++) {
                    System.out.println("0x"+Long.toHexString(pieceOnSquareRandom[color | piece][square])+"L,");
                }
            }
        }

    }
}
